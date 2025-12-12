# ADR-001: Biometric Sync Integration Architecture

**Status**: ACCEPTED (with recommended improvements)
**Date**: 2025-12-06
**Decision Maker**: Architecture Review Team
**Stakeholders**: Backend Team, DevOps, Data Engineering

---

## Context

The HRMS system requires integration with external biometric devices (ESSL) to capture attendance punch data. The current architecture needs to handle:

1. **Data Source**: Supabase cloud database storing biometric records
2. **Data Format**: CSV-encoded data within JSON responses from REST API
3. **Volume**: Potentially thousands of records per sync cycle
4. **Frequency**: Daily/periodic syncs across multiple companies and employees
5. **Reliability**: Must handle partial failures and data inconsistencies
6. **Audit Trail**: Maintain traceability of sync operations for compliance

### Current System State

- Attendance Module fully implemented with entities: PunchLog, DailyAttendance, Shift, Holiday, etc.
- GraphQL API ready to expose biometric sync operations
- Basic sync service implemented using RestTemplate
- No staging mechanism currently in place

### Key Challenges

1. **Large Dataset Handling**: Loading entire result sets into memory for processing
2. **Failure Recovery**: No checkpoint mechanism to resume failed syncs
3. **Partial Success Handling**: Unclear state if sync partially completes
4. **Retry Safety**: Risk of duplicate records if retry logic engaged
5. **Monitoring Gaps**: No visibility into sync performance or stuck records

---

## Decision

### Primary Decision: Implement Multi-Staged Sync Architecture with Staging Table

**Adopted Pattern**: Extract-Transform-Load (ETL) with staging layer

```
┌────────────────────────────────────────────────────────────┐
│ Stage 1: FETCH & PERSIST TO STAGING                       │
├────────────────────────────────────────────────────────────┤
│ - REST call to Supabase API                                │
│ - Persist raw records to biometric_sync_staging table      │
│ - Status: PENDING, track sync_batch_id                     │
└────────┬─────────────────────────────────────────────────┬┘
         │                                                   │
         ▼                                                   ▼
    ┌──────────────────┐                         ┌──────────────────┐
    │ Success: Mark    │                         │ Failure: Keep    │
    │ PROCESSED        │                         │ for manual retry  │
    └──────────────────┘                         └──────────────────┘
         │
         ▼
┌────────────────────────────────────────────────────────────┐
│ Stage 2: BATCH PROCESSING WITH TRANSACTIONS                │
├────────────────────────────────────────────────────────────┤
│ - Read from staging table (status = PENDING)               │
│ - Batch size: 1000 records/transaction                     │
│ - Atomic operations: Parse → Validate → Dedup → Insert    │
│ - Checkpoint after each batch success                      │
└────────┬─────────────────────────────────────────────────┬┘
         │                                                   │
         ▼                                                   ▼
    ┌──────────────────────┐                    ┌──────────────────────┐
    │ Successful Records:  │                    │ Failed Records:      │
    │ - Insert to         │                    │ - Update status      │
    │   punch_logs        │                    │   to FAILED          │
    │ - Mark PROCESSED    │                    │ - Log error message  │
    │   in staging        │                    │ - Retain for retry   │
    └──────────────────────┘                    └──────────────────────┘
         │
         ▼
┌────────────────────────────────────────────────────────────┐
│ Stage 3: MONITORING & AUDIT                                │
├────────────────────────────────────────────────────────────┤
│ - Track sync_batch metrics (duration, counts)              │
│ - Query stuck/failed records for manual intervention       │
│ - Support resumption from last checkpoint                  │
└────────────────────────────────────────────────────────────┘
```

### Sub-Decisions

#### 1. Staging Table Schema
```sql
CREATE TABLE biometric_sync_staging (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    tenant_id VARCHAR(50) NOT NULL,
    company_id BIGINT,
    supabase_id VARCHAR(255) NOT NULL UNIQUE,
    device_data TEXT NOT NULL,
    raw_data JSONB,
    sync_batch_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSED, FAILED, DUPLICATE
    error_message TEXT,
    attempted_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,

    INDEX idx_staging_tenant_batch (tenant_id, sync_batch_id),
    INDEX idx_staging_status (status),
    INDEX idx_staging_supabase_id (supabase_id),
    UNIQUE (supabase_id, tenant_id) -- Prevent re-ingestion
);

CREATE TABLE biometric_sync_batch (
    id VARCHAR(50) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    company_id BIGINT,
    sync_type VARCHAR(20) NOT NULL, -- FULL, INCREMENTAL
    from_date DATE,
    to_date DATE,
    total_fetched INT,
    total_staged INT,
    total_processed INT,
    total_failed INT,
    status VARCHAR(20) NOT NULL, -- INITIATED, IN_PROGRESS, COMPLETED, FAILED
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    error_message TEXT,

    INDEX idx_batch_tenant_status (tenant_id, status)
);
```

#### 2. Batch Processing Strategy
- **Batch Size**: 1000 records per transaction (configurable)
- **Transaction Scope**: Each batch wrapped in @Transactional
- **Concurrency**: Single-threaded per company to maintain order
- **Retry Logic**: Exponential backoff (1s, 2s, 4s, 8s) for failed batches
- **Max Retries**: 3 attempts per batch before marking batch as FAILED

#### 3. Duplicate Detection Enhancement
- Use Supabase `id` field as unique key in staging table
- Check both staging and punch_logs tables
- 1-minute window for same-minute duplicates (existing logic)
- Deduplicate at staging insert (prevents DB constraint violations)

#### 4. Employee Matching With Caching
```java
// Persist employee lookup cache to allow resumption
Map<String, Long> employeeBiometricCache = new HashMap<>();

// On first run:
// 1. Load all employees with biometricId into cache
// 2. Save cache serialization timestamp
// 3. Use for subsequent batch processing
// 4. Invalidate if employee data changes
```

#### 5. Async Processing Option (Phase 2)
```java
// Trigger async job for large syncs (>10K records)
@Async
public CompletableFuture<BiometricSyncResult> syncFromSupabaseAsync(
    String tenantId, Long companyId) {
    // Process via job queue, poll for status
}
```

---

## Current Implementation vs. Proposed

### Current Architecture (In-Memory Only)

**Flow**:
```
Supabase REST API
    ↓
List<Map> in-memory buffer
    ↓ for-each loop
Direct insertion to punch_logs
```

**Problems**:
| Issue | Severity | Impact |
|-------|----------|--------|
| No resumption point | HIGH | Loss of sync state on failure |
| Memory bloat (100K+ records) | MEDIUM | Potential OOM exceptions |
| No audit trail | MEDIUM | Can't trace which records synced |
| Single transaction | HIGH | All-or-nothing; partial failure unclear |
| Duplicate risk on retry | HIGH | Records may be inserted twice |
| No monitoring/metrics | MEDIUM | Blind to sync health |
| Blocks GraphQL server | MEDIUM | Long sync times = timeout risk |

### Proposed Architecture (Staged)

**Flow**:
```
Supabase REST API
    ↓
Staging Table (Persist)
    ↓
Batch Job (1000 records/transaction)
    ↓
Dedup + Parse + Validate
    ↓
Insert to punch_logs (if no errors)
    ↓
Mark staging record PROCESSED
```

**Benefits**:
| Benefit | Impact |
|---------|--------|
| Resumable from checkpoint | Can restart at batch N+1 |
| Memory efficient | Only 1000 records in memory at once |
| Full audit trail | Track every sync attempt |
| Atomic batches | Clear success/failure per batch |
| Safe retries | Staging prevents duplicates |
| Monitoring ready | Track batch metrics |
| Async capable | Don't block GraphQL server |

---

## Consequences

### Positive

1. **Reliability**: Failed syncs can be resumed without data loss
2. **Observability**: Clear metrics on sync health (batch counts, error rates)
3. **Scalability**: Batch processing supports large datasets (1M+ records)
4. **Data Integrity**: Staging layer acts as deduplication shield
5. **Auditability**: Complete history of all sync operations
6. **Flexibility**: Easy to add async processing later
7. **Debugging**: Stuck records can be inspected and retried manually

### Negative

1. **Added Complexity**: Staging layer requires additional code
2. **Storage Overhead**: Temporary storage of raw records (mitigated by purging after 30 days)
3. **Latency**: Two-hop process (staging → punch_logs) vs. direct insert
4. **Operational Burden**: New tables to monitor and maintain
5. **Migration Effort**: Current in-memory code requires refactor

### Risks

1. **Staging Table Growth**: Without purge policy, disk usage could grow unbounded
   - **Mitigation**: Implement automatic purge of PROCESSED records after 30 days

2. **Stale Cache**: Employee biometricId mappings could become outdated
   - **Mitigation**: Refresh cache on each sync cycle

3. **Batch Size Tuning**: 1000 records may not be optimal for all environments
   - **Mitigation**: Make configurable, benchmark different sizes

---

## Alternatives Considered

### Alternative 1: Keep Current In-Memory Approach
**Pros**: Simple, no additional tables
**Cons**: All issues listed above remain
**Verdict**: **REJECTED** - Does not meet reliability requirements

### Alternative 2: Stream Processing with Kafka
**Pros**: Decoupled, can handle massive scale
**Cons**: Overkill for current volume, operational complexity
**Verdict**: **REJECTED** - Premature optimization; revisit at 100K+ daily records

### Alternative 3: Webhook-based Push from Supabase
**Pros**: Real-time data, no polling
**Cons**: Requires Supabase webhook support (may not be available), network complexity
**Verdict**: **DEFERRED** - Research feasibility for Phase 2

### Alternative 4: Direct Database Replication
**Pros**: Automatic sync, minimal code
**Cons**: Coupling HRMS DB to external Supabase, security risks
**Verdict**: **REJECTED** - Violates data boundary principles

---

## Implementation Plan

### Phase 1: Staging Infrastructure (Sprint 1)
- [ ] Create `biometric_sync_staging` and `biometric_sync_batch` tables
- [ ] Add migration scripts
- [ ] Update schema documentation

### Phase 2: Service Refactoring (Sprint 1-2)
- [ ] Refactor `BiometricSyncServiceImpl` to use staging layer
- [ ] Implement batch processing logic
- [ ] Add checkpoint/resumption support
- [ ] Update error handling

### Phase 3: Monitoring & Operations (Sprint 2)
- [ ] Add GraphQL queries for sync metrics
- [ ] Create dashboard for stuck records
- [ ] Implement 30-day staging purge job
- [ ] Add alerts for failed batches

### Phase 4: Testing & Validation (Sprint 2-3)
- [ ] Load test with 100K+ records
- [ ] Failure scenario testing
- [ ] Data integrity verification
- [ ] Performance benchmarking

### Phase 5: Async Processing (Phase 2, Future)
- [ ] Implement Spring @Async for large syncs
- [ ] Add job queue and status polling
- [ ] Decouple sync from GraphQL request thread

---

## Configuration

```properties
# Biometric Sync Configuration
biometric.sync.batch-size=1000
biometric.sync.max-retries=3
biometric.sync.retention-days=30
biometric.sync.async-threshold=5000  # Records; trigger async if > this
biometric.sync.timeout-minutes=15

# Supabase Configuration (existing)
supabase.url=https://ehqejpobihorcbabqgrf.supabase.co
supabase.key=${SUPABASE_KEY}  # Should be env var in prod
supabase.table=biometric_data
```

---

## Monitoring & Metrics

### GraphQL Queries to Expose

```graphql
query {
  syncBatchStatus(batchId: "BATCH_20251206_001") {
    batchId
    tenantId
    status
    totalFetched
    totalProcessed
    totalFailed
    startedAt
    completedAt
    errorMessage
  }

  syncMetrics(tenantId: "TENANT001", fromDate: "2025-12-01", toDate: "2025-12-06") {
    totalSyncs
    successCount
    failureCount
    averageRecordsPerSync
    totalRecordsProcessed
  }

  failedSyncRecords(tenantId: "TENANT001", limit: 100) {
    supabaseId
    errorMessage
    attemptedCount
    createdAt
  }
}
```

### Metrics to Track

- **Sync Duration**: Time from fetch to completion
- **Batch Success Rate**: Percentage of batches processed successfully
- **Duplicate Detection Rate**: How many duplicates found
- **Employee Not Found Rate**: Missing employee matches
- **Average Records per Batch**: For tuning batch size
- **Staging Table Size**: Monitor disk usage growth
- **Retry Count Distribution**: How many retries needed

---

## Security Considerations

1. **Staging Data Sensitivity**: Raw biometric data stored in staging table
   - Mitigation: Encrypt JSONB columns at rest (PostgreSQL pgcrypto)
   - Implement field-level access control on staging tables

2. **API Key in Config**: Supabase key currently in properties
   - Mitigation: Move to environment variables or secure vault (e.g., AWS Secrets Manager)
   - Implement key rotation policy

3. **Batch Processing Isolation**: Ensure batch jobs respect tenant boundaries
   - Mitigation: Always filter by tenantId on all staging queries

---

## References

- Commit: `7bea142` - Initial Biometric Sync Integration
- Entity Docs: `PunchLog.java`, `DailyAttendance.java`
- Service Docs: `BiometricSyncService.java`, `BiometricSyncServiceImpl.java`
- Config: `SupabaseConfig.java`, `application.properties`

---

## Approval & Sign-off

- **Architecture Review**: APPROVED
- **Development Lead**: PENDING
- **DevOps Lead**: PENDING
- **Database Administrator**: PENDING

---

**Next Review**: 2025-12-20 (post-Phase 1 completion)
