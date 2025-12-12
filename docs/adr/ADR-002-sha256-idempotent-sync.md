# ADR-002: SHA256-Based Idempotent Biometric Sync

**Status**: PROPOSED

**Date**: 2025-12-12

**Decision Maker**: Architecture Review Team

**Stakeholders**: Backend Team, DevOps, Database Administrator, Security Team

---

## Context

### Current System Limitations

The HRMS system integrates with Supabase cloud to fetch biometric punch data from ESSL devices. Critical architectural limitations exist:

#### 1. Full Table Scan Every Sync (Performance Problem)
```java
// Current: BiometricSyncServiceImpl.java:228-260
String url = supabaseConfig.getBiometricDataEndpoint() + "?select=*&order=created_at.desc";
// Fetches ALL rows every sync
```

**Impact**:
- As Supabase grows (100K+ records), sync time increases linearly
- Network bandwidth waste (re-downloading processed records)
- Memory pressure (entire dataset loaded into heap)

#### 2. Fragile Time-Window Deduplication (Reliability Problem)
```java
// Current: ±1 minute window check
private boolean isDuplicatePunch(Long employeeId, OffsetDateTime punchTime, String supabaseId) {
    List<PunchLog> existing = punchLogRepository.findByEmployeeIdAndPunchTimeBetween(
        employeeId, punchTime.minusMinutes(1), punchTime.plusMinutes(1));
    return !existing.isEmpty();
}
```

**Problems**:
- Arbitrary 1-minute window (what if device clock skews by 2 minutes?)
- Supabase ID stored in rawData JSONB but **NOT indexed**
- No source-specific deduplication
- Manual punches at same time incorrectly flagged as duplicates

#### 3. Read-Only Integration (Data Integrity Problem)

- Supabase has `sync_status` column but HRMS never updates it
- No feedback loop: If sync fails, Supabase doesn't know
- Cannot implement incremental sync

#### 4. Lack of Idempotency (Operational Problem)

- Sync runs twice accidentally → duplicates inserted
- No deterministic way to prevent duplicates
- Manual cleanup required

### Supabase Schema (Confirmed)

```sql
create table public.biometric_data (
  id character varying not null,              -- SHA256(device_data)
  device_data character varying null,
  created_at timestamp with time zone not null default now(),
  sync_status character varying null,         -- NULL | 'synced' | 'reject-duplicate'
  synced_at timestamp without time zone null,
  constraint biometric_data_pkey primary key (id)
);
```

**Key Insight**: Supabase uses SHA256 hash of `device_data` as primary key - this is the natural deduplication key.

---

## Decision

### Primary Decision: SHA256-Based Idempotent Sync with Bi-Directional Integration

**Pattern**: Content-Addressable Storage + Incremental ETL + Eventual Consistency

```
┌──────────────────────────────────────────────────────────────┐
│ SUPABASE CLOUD                                               │
│ biometric_data (sync_status=NULL)                            │
└────────────┬─────────────────────────────────────────────────┘
             │ GET ?sync_status=is.null&limit=1000
             ▼
┌──────────────────────────────────────────────────────────────┐
│ HRMS BACKEND                                                 │
│ 1. Check sha256 (PRIMARY deduplication - O(1) lookup)       │
│ 2. Parse device_data, match employee                        │
│ 3. Insert to punch_logs (sha256 UNIQUE)                     │
│ 4. PATCH Supabase → sync_status='synced'/'reject-duplicate' │
└────────────┬─────────────────────────────────────────────────┘
             │ Batch: 100 records/transaction
             ▼
┌──────────────────────────────────────────────────────────────┐
│ POSTGRESQL                                                   │
│ punch_logs (sha256 VARCHAR(64) UNIQUE)                       │
└──────────────────────────────────────────────────────────────┘
```

### Sub-Decisions

#### Decision 1: Two-Tier Deduplication Strategy

**PRIMARY**: SHA256 content hash (for all non-HR_MANUAL sources)
- Fast O(1) lookup via unique index
- 100% reliable (SHA256 collision: 2^-256 ≈ negligible)
- Source-agnostic (works for SUPABASE, REST_API, IMPORT, USER_PORTAL, MOBILE)

**SECONDARY**: Time window ±1 minute (fallback for HR_MANUAL entries)
- HR_MANUAL punches don't have sha256 (web form entry)
- Existing time-window logic preserved for backward compatibility

#### Decision 2: PunchSource Enum Expansion

**New Values**:
```java
public enum PunchSource {
    SUPABASE,      // Biometric devices synced via Supabase (sha256 from Supabase)
    HR_MANUAL,     // HR/Admin entered via web portal (sha256 = NULL)
    USER_PORTAL,   // Employee self-service portal (sha256 calculated locally)
    REST_API,      // External API integrations (sha256 calculated locally)
    IMPORT,        // Bulk CSV/Excel imports (sha256 calculated per row)
    MOBILE,        // Mobile app punches (sha256 calculated locally)
    LEGACY         // Pre-ADR-002 data (migration only, may delete later)
}
```

**Migration Strategy**:
```sql
-- Mark existing BIOMETRIC entries as LEGACY
UPDATE punch_logs SET punch_source = 'LEGACY' WHERE punch_source = 'BIOMETRIC';
```

#### Decision 3: SHA256 Column Specification

**Schema**:
```sql
ALTER TABLE punch_logs ADD COLUMN sha256 VARCHAR(64);
CREATE UNIQUE INDEX idx_punch_logs_sha256 ON punch_logs(sha256) WHERE sha256 IS NOT NULL;
```

**Design Choices**:
- **Nullable**: HR_MANUAL entries don't have SHA256
- **Unique Constraint**: Enforces idempotency at database level
- **Partial Index**: Only non-NULL values indexed (saves space)
- **64 Characters**: SHA256 hex digest = 64 chars (256 bits / 4 bits per hex char)

#### Decision 4: Batch Processing with Transactions

**Batch Size**: 100 records per transaction
- Large enough to reduce commit overhead
- Small enough to avoid long lock times
- Memory footprint: ~10KB/record × 100 = 1MB per batch

**Commit Frequency**: 10 batches of 100 = 1000 records total per sync

#### Decision 5: Write-Back Implementation (Eventual Consistency)

**Supabase API**: PATCH method
```http
PATCH /rest/v1/biometric_data?id=eq.{sha256}
{
  "sync_status": "synced",
  "synced_at": "2025-12-12T08:05:23+05:30"
}
```

**Error Handling**: Eventual consistency model
- If PATCH fails: Log warning, don't throw exception
- Punch still inserted in HRMS (data is priority)
- Next sync re-fetches same record → duplicate check catches it → marks 'reject-duplicate'

#### Decision 6: Incremental Sync Query

**Old**: `GET /biometric_data?select=*&order=created_at.desc`

**New**: `GET /biometric_data?select=*&sync_status=is.null&order=created_at.asc&limit=1000`

**Benefits**:
- Only unprocessed records transferred
- Oldest-first processing (FIFO)
- Bounded memory (1000 max)

---

## Comparison: Current vs Proposed

| Aspect | Current (In-Memory) | Proposed (SHA256 + Write-Back) |
|--------|---------------------|--------------------------------|
| **Deduplication** | Time window ±1 min | SHA256 hash (O(1) lookup) |
| **Fetch Query** | `?select=*` (all rows) | `?sync_status=is.null&limit=1000` |
| **Idempotency** | NO (risky retries) | YES (unique constraint enforced) |
| **Supabase Write-Back** | NO | YES (eventual consistency) |
| **Batch Size** | In-memory (all) | 100 records/transaction |
| **Memory Footprint** | O(n) - entire dataset | O(1) - 100 records max |
| **Duplicate Check** | 2 DB queries per punch | 1 index lookup per punch |
| **Source Tracking** | Generic "BIOMETRIC" | Specific: SUPABASE, HR_MANUAL, etc. |
| **Performance (10K records)** | ~5-10 minutes | ~2-3 minutes |

---

## Consequences

### Positive

1. **Idempotency Guarantee**
   - SHA256 unique constraint makes duplicate insertion impossible
   - Safe to retry syncs without manual cleanup
   - Supports distributed sync jobs (multiple servers)

2. **Performance Improvement**
   - Incremental fetch: Only new records transferred
   - Index lookup vs time-window scan: O(1) vs O(n)
   - Estimated 50-70% reduction in sync time

3. **Operational Visibility**
   - Supabase `sync_status` provides audit trail
   - Can query Supabase to see pending records
   - Failure diagnostics: Check which records failed

4. **Data Integrity**
   - Database-level unique constraint prevents errors
   - No race conditions (two threads inserting same punch)
   - Source-specific deduplication (HR_MANUAL vs SUPABASE)

5. **Scalability**
   - Batch processing limits memory usage (constant footprint)
   - Can sync 100K+ records without OOM
   - Supports future async processing

### Negative

1. **Increased Complexity**
   - Two-way communication (GET + PATCH) vs one-way (GET only)
   - SHA256 calculation overhead (negligible: ~1ms per record)
   - More error paths to handle

2. **Schema Migration Risk**
   - Adding unique constraint requires backfill
   - Potential downtime (mitigated by online ALTER)
   - Rollback complexity

3. **Eventual Consistency Trade-Off**
   - If PATCH fails, Supabase won't know sync succeeded
   - Could lead to "zombie" records (synced in HRMS, NULL in Supabase)
   - Mitigation: Scheduled reconciliation job (weekly)

4. **Dependency on Supabase Write Access**
   - Requires SERVICE_ROLE key (more permissions)
   - Security risk if key leaked
   - Mitigation: Store in environment variable, rotate quarterly

### Risks & Mitigations

| Risk | Severity | Mitigation |
|------|----------|------------|
| **Two-phase commit failure** (insert OK, PATCH fails) | MEDIUM | Eventual consistency: Next sync catches it → marks 'reject-duplicate' |
| **SHA256 collision** | CRITICAL | Negligible (2^-256 probability) - no mitigation needed |
| **Supabase write permission** (key compromised) | HIGH | Store in AWS Secrets Manager, rotate quarterly |
| **Batch transaction rollback** | MEDIUM | Process individually on exception, continue batch |
| **Legacy data migration** | MEDIUM | Backfill from rawData, mark LEGACY, delete after validation |

---

## Alternatives Considered

### Alternative 1: Keep Current Time-Window Deduplication
**Pros**: No schema changes, no migration risk
**Cons**: All current problems remain
**Verdict**: **REJECTED** - Does not address fundamental issues

### Alternative 2: Composite Key (employee_id + punch_time) as Unique
**Pros**: Simpler than SHA256
**Cons**: Breaks when employee has punches at same second, no content-addressable property
**Verdict**: **REJECTED** - Too restrictive, false positives likely

### Alternative 3: UUID Generated by HRMS
**Pros**: Standard practice
**Cons**: Can't deduplicate BEFORE insertion, Supabase already uses SHA256 (redundant)
**Verdict**: **REJECTED** - Doesn't leverage existing architecture

### Alternative 4: Queue-Based Async Sync with Message Broker
**Pros**: Fully decoupled, horizontal scaling
**Cons**: Massive operational overhead (Kafka/RabbitMQ), overkill for current volume
**Verdict**: **DEFERRED** - Revisit at 100K+ records/day

### Alternative 5: Direct Database Replication (PostgreSQL FDW)
**Pros**: No application code
**Cons**: Tight coupling, security nightmare, no transformation logic
**Verdict**: **REJECTED** - Violates separation of concerns

---

## Implementation Outline (High-Level)

### Phase 1: Schema Migration
- Create migration script: `V002__add_sha256_to_punch_logs.sql`
- Add `sha256 VARCHAR(64)` column (nullable)
- Backfill sha256 for existing BIOMETRIC entries
- Create unique index: `idx_punch_logs_sha256`

### Phase 2: Entity & Enum Updates
- Update `PunchSource` enum (add SUPABASE, HR_MANUAL, USER_PORTAL, REST_API, IMPORT, MOBILE, LEGACY)
- Update `PunchLog` entity (add sha256 field, validation)

### Phase 3: Repository Methods
- Add `existsBySha256(String sha256)`
- Add `findBySha256(String sha256)`

### Phase 4: Service Layer Refactoring
- Modify `fetchFromSupabase()`: Add `?sync_status=is.null&limit=1000`
- Add `updateSupabaseStatus()` method (PATCH)
- Update `syncFromSupabase()`: SHA256 dedup + batch processing
- Add `processBatch()` method (100 records/txn)

### Phase 5: Testing
- Unit tests: SHA256Calculator, deduplication logic
- Integration tests: Idempotent sync, write-back failure scenarios
- Load tests: 10K+ records

### Phase 6: Monitoring
- GraphQL metrics queries
- Prometheus/Grafana alerts
- Structured logging

---

## Security Considerations

1. **Supabase API Key Management**
   - **Current**: Hardcoded in application.properties (INSECURE)
   - **Recommended**: Environment variable or AWS Secrets Manager
   - **Rotation**: Quarterly minimum

2. **Write Permission Validation**
   - Verify SERVICE_ROLE key has PATCH access
   - Test on staging before production

3. **SQL Injection Prevention**
   - JPA Repository methods use prepared statements (SAFE)
   - No raw SQL with user input

4. **Data Encryption at Rest**
   - Future: Enable PostgreSQL pgcrypto for sensitive columns

---

## Monitoring & Metrics

### Key Metrics

| Metric | Target | Alert Threshold |
|--------|--------|-----------------|
| Sync duration | <3 min | >5 min |
| Duplicate rate | <5% | >10% |
| Write-back failure rate | <1% | >5% |
| SHA256 collision count | 0 (always) | >0 (critical) |
| Employee not found rate | <2% | >5% |

### GraphQL Queries (Future)

```graphql
query {
  biometricSyncMetrics(
    tenantId: "TENANT001"
    fromDate: "2025-12-01"
    toDate: "2025-12-12"
  ) {
    totalSyncs
    totalRecordsProcessed
    totalDuplicatesDetected
    sha256CollisionCount
  }

  punchCountBySource(tenantId: "TENANT001") {
    source
    count
  }
}
```

---

## Rollback Plan

### Emergency Rollback (<2 hours)

**Step 1**: Disable unique constraint
```sql
DROP INDEX idx_punch_logs_sha256;
```

**Step 2**: Revert code
```bash
git revert <commit-hash>
git push origin feature/attendance-leave-backend-data
```

**Step 3**: Data cleanup (if needed)
```sql
ALTER TABLE punch_logs DROP COLUMN sha256;
UPDATE punch_logs SET punch_source = 'BIOMETRIC' WHERE punch_source = 'LEGACY';
```

---

## Approval & Sign-Off

- **Architecture Review**: PENDING
- **Development Lead**: PENDING
- **DevOps Lead**: PENDING
- **Database Administrator**: PENDING
- **Security Team**: PENDING

---

## Next Steps

1. **Immediate**: Review ADR-002 for approval
2. **Post-Approval**: Create detailed implementation plan (line-by-line code changes)
3. **Week 1**: Schema migration + entity updates
4. **Week 2-3**: Service refactoring + testing
5. **Week 4**: Production deployment

**Review Date**: 2025-12-26 (2 weeks post-implementation)

**Success Criteria**:
- Zero duplicate punches in production (24 hours validation)
- Sync time <3 minutes for 1000 records
- Write-back success rate >95%
- No SHA256 collisions detected (lifetime)

---

**References**:
- ADR-001: Biometric Sync Integration Architecture
- Supabase Schema: `biometric_data` table
- Commit: `7bea142` - Initial Biometric Sync Integration
- Commit: `55200ba` - Time Center APIs

---

**Model**: Claude Sonnet 4.5 (claude-sonnet-4-5-20250929)
**Date**: 2025-12-12
