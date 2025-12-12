# ADR-002 Implementation Plan: SHA256-Based Idempotent Biometric Sync

**Status**: APPROVED - Ready for Implementation
**Start Date**: 2025-12-12
**Estimated Duration**: 4 weeks
**Model**: Claude Sonnet 4.5

---

## Implementation Phases

### Phase 1: Database Schema Migration (Day 1-2)
### Phase 2: Enum & Entity Updates (Day 3-4)
### Phase 3: Repository Layer (Day 4)
### Phase 4: Utility Classes (Day 5)
### Phase 5: Service Layer Refactoring (Week 2)
### Phase 6: Configuration Updates (Week 2)
### Phase 7: Testing (Week 3)
### Phase 8: Deployment (Week 4)

---

## PHASE 1: Database Schema Migration

### Step 1.1: Create Migration SQL Script

**File**: `src/main/resources/db/migration/V002__add_sha256_to_punch_logs.sql`

**Action**: Create new file

**Content**:
```sql
-- ADR-002: Add SHA256 deduplication support to punch_logs
-- Date: 2025-12-12
-- Author: Architecture Team

-- Step 1: Add sha256 column (nullable for safe migration)
ALTER TABLE punch_logs
ADD COLUMN sha256 VARCHAR(64);

-- Step 2: Add index for source-based analytics
CREATE INDEX idx_punch_logs_source
ON punch_logs(punch_source, created_at);

-- Step 3: Backfill sha256 from existing rawData JSONB (if available)
-- Note: This extracts SHA256 from device_data field in rawData
UPDATE punch_logs
SET sha256 = encode(digest(raw_data->>'deviceData', 'sha256'), 'hex')
WHERE punch_source = 'BIOMETRIC'
  AND raw_data->>'deviceData' IS NOT NULL
  AND sha256 IS NULL;

-- Step 4: Create unique index on sha256 (partial index - only non-NULL values)
-- This enforces idempotency at database level
CREATE UNIQUE INDEX idx_punch_logs_sha256
ON punch_logs(sha256)
WHERE sha256 IS NOT NULL;

-- Step 5: Update enum constraint to support new PunchSource values
-- Note: Spring JPA handles enum mapping, but for manual SQL operations
ALTER TABLE punch_logs DROP CONSTRAINT IF EXISTS punch_logs_punch_source_check;
ALTER TABLE punch_logs ADD CONSTRAINT punch_logs_punch_source_check
CHECK (punch_source IN (
    'SUPABASE',      -- New: Biometric devices via Supabase
    'HR_MANUAL',     -- New: HR/Admin web portal entry
    'USER_PORTAL',   -- New: Employee self-service portal
    'REST_API',      -- New: External API integrations
    'IMPORT',        -- Replaces EXCEL_IMPORT
    'MOBILE',        -- Existing
    'LEGACY',        -- New: Pre-ADR-002 data
    'BIOMETRIC',     -- Deprecated (will migrate to LEGACY)
    'PORTAL',        -- Deprecated
    'EXCEL_IMPORT',  -- Deprecated (will migrate to IMPORT)
    'MANUAL',        -- Deprecated (will migrate to HR_MANUAL)
    'API'            -- Deprecated
));

-- Step 6: Migrate existing data to new enum values
-- Mark all BIOMETRIC entries as LEGACY for now
UPDATE punch_logs
SET punch_source = 'LEGACY'
WHERE punch_source = 'BIOMETRIC';

-- Optional: Migrate other deprecated values
UPDATE punch_logs SET punch_source = 'HR_MANUAL' WHERE punch_source = 'MANUAL';
UPDATE punch_logs SET punch_source = 'IMPORT' WHERE punch_source = 'EXCEL_IMPORT';

-- Step 7: Add comments for documentation
COMMENT ON COLUMN punch_logs.sha256 IS 'SHA256 hash of device_data for idempotent deduplication (ADR-002). NULL only for HR_MANUAL entries.';
COMMENT ON INDEX idx_punch_logs_sha256 IS 'Unique index for SHA256-based deduplication. Partial index excludes NULL values.';

-- Migration complete
```

**Validation**:
```sql
-- Test 1: Verify column added
SELECT column_name, data_type, character_maximum_length, is_nullable
FROM information_schema.columns
WHERE table_name = 'punch_logs' AND column_name = 'sha256';
-- Expected: sha256 | character varying | 64 | YES

-- Test 2: Verify unique index created
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'punch_logs' AND indexname = 'idx_punch_logs_sha256';
-- Expected: CREATE UNIQUE INDEX ... WHERE sha256 IS NOT NULL

-- Test 3: Verify NULL sha256 allowed (for HR_MANUAL)
INSERT INTO punch_logs (tenant_id, employee_id, punch_time, punch_type, punch_source, status, sha256)
VALUES ('TEST', 1, NOW(), 'IN', 'HR_MANUAL', 'MATCHED', NULL);
-- Expected: Success

-- Test 4: Verify duplicate sha256 rejected
INSERT INTO punch_logs (tenant_id, employee_id, punch_time, punch_type, punch_source, status, sha256)
VALUES ('TEST', 1, NOW(), 'IN', 'SUPABASE', 'MATCHED', 'abc123def456');

INSERT INTO punch_logs (tenant_id, employee_id, punch_time, punch_type, punch_source, status, sha256)
VALUES ('TEST', 2, NOW(), 'OUT', 'SUPABASE', 'MATCHED', 'abc123def456');
-- Expected: ERROR - duplicate key value violates unique constraint

-- Cleanup test data
DELETE FROM punch_logs WHERE tenant_id = 'TEST';
```

**Execution**:
```bash
# Backup database first
pg_dump -h localhost -U postgres hrmsdb > backup_before_adr002_$(date +%Y%m%d_%H%M%S).sql

# Test on staging database first
PGPASSWORD=Admin@123 psql -h localhost -U postgres -d hrmsdb_staging -f src/main/resources/db/migration/V002__add_sha256_to_punch_logs.sql

# If successful, run on production
PGPASSWORD=Admin@123 psql -h localhost -U postgres -d hrmsdb -f src/main/resources/db/migration/V002__add_sha256_to_punch_logs.sql
```

**Rollback** (if needed):
```sql
-- Emergency rollback script
DROP INDEX IF EXISTS idx_punch_logs_sha256;
DROP INDEX IF EXISTS idx_punch_logs_source;
ALTER TABLE punch_logs DROP COLUMN IF EXISTS sha256;
UPDATE punch_logs SET punch_source = 'BIOMETRIC' WHERE punch_source = 'LEGACY';
```

---

## PHASE 2: Update PunchSource Enum

### Step 2.1: Update PunchSource.java

**File**: `src/main/java/com/hrms/enums/PunchSource.java`

**Current State**: Read file first
```bash
cat src/main/java/com/hrms/enums/PunchSource.java
```

**Action**: Replace entire file content

**New Content**:
```java
package com.hrms.enums;

/**
 * Source of attendance punch entry.
 * Updated per ADR-002 for SHA256-based deduplication.
 *
 * @since 1.0
 * @version 2.0 (ADR-002)
 */
public enum PunchSource {
    /**
     * Biometric devices synced via Supabase cloud.
     * SHA256 hash provided by Supabase (device_data content hash).
     * Replaces deprecated BIOMETRIC enum.
     */
    SUPABASE,

    /**
     * HR/Admin manual entry via web portal.
     * No SHA256 (no device_data), uses time-window deduplication.
     * Replaces deprecated MANUAL enum.
     */
    HR_MANUAL,

    /**
     * Employee self-service portal punches.
     * SHA256 calculated locally from form submission.
     * Replaces deprecated WEB/PORTAL enums.
     */
    USER_PORTAL,

    /**
     * External REST API integrations.
     * SHA256 calculated locally from API request payload.
     */
    REST_API,

    /**
     * Bulk Excel/CSV imports.
     * SHA256 calculated per row from import data.
     * Replaces deprecated EXCEL_IMPORT enum.
     */
    IMPORT,

    /**
     * Mobile app punches.
     * SHA256 calculated from mobile API payload.
     */
    MOBILE,

    /**
     * Legacy data from pre-ADR-002 system.
     * Used only for migration; will be deleted after validation.
     * Previously BIOMETRIC entries.
     */
    LEGACY,

    // ========== DEPRECATED VALUES (Backward Compatibility) ==========

    /**
     * @deprecated Use {@link #SUPABASE} instead.
     * Kept for backward compatibility during migration.
     * Will be removed in version 3.0.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    BIOMETRIC,

    /**
     * @deprecated Use {@link #USER_PORTAL} instead.
     * Kept for backward compatibility during migration.
     * Will be removed in version 3.0.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    PORTAL,

    /**
     * @deprecated Use {@link #IMPORT} instead.
     * Kept for backward compatibility during migration.
     * Will be removed in version 3.0.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    EXCEL_IMPORT,

    /**
     * @deprecated Use {@link #HR_MANUAL} instead.
     * Kept for backward compatibility during migration.
     * Will be removed in version 3.0.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    MANUAL,

    /**
     * @deprecated Use {@link #REST_API} instead.
     * Kept for backward compatibility during migration.
     * Will be removed in version 3.0.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    API;

    /**
     * Check if this source requires SHA256 hash.
     *
     * @return true if SHA256 is required, false if nullable
     */
    public boolean requiresSha256() {
        return this != HR_MANUAL && this != LEGACY && this != MANUAL;
    }

    /**
     * Get the modern equivalent of a deprecated source.
     *
     * @return Modern PunchSource enum, or self if already modern
     */
    public PunchSource getModernEquivalent() {
        return switch (this) {
            case BIOMETRIC -> SUPABASE;
            case MANUAL -> HR_MANUAL;
            case PORTAL -> USER_PORTAL;
            case EXCEL_IMPORT -> IMPORT;
            case API -> REST_API;
            default -> this;
        };
    }
}
```

**Validation**:
```bash
# Compile to check for syntax errors
/home/sysadmin/tools/apache-maven-3.9.6/bin/mvn clean compile

# Expected: BUILD SUCCESS
```

---

## PHASE 3: Update PunchLog Entity

### Step 3.1: Update PunchLog.java

**File**: `src/main/java/com/hrms/entity/PunchLog.java`

**Action**: Add sha256 field and validation

**Add after line 95** (after rawData field):

```java
/**
 * SHA256 hash of device_data for idempotent deduplication (ADR-002).
 * - NULL for HR_MANUAL entries (no device_data)
 * - Non-null for SUPABASE, REST_API, IMPORT, USER_PORTAL, MOBILE
 * - Indexed with UNIQUE constraint (prevents duplicates)
 *
 * @see com.hrms.util.SHA256Calculator
 */
@Column(name = "sha256", length = 64, unique = true)
private String sha256;
```

**Update @Table indexes** (replace lines 26-31):

```java
@Table(name = "punch_logs",
    indexes = {
        @Index(name = "idx_punch_logs_tenant", columnList = "tenant_id"),
        @Index(name = "idx_punch_logs_tenant_date", columnList = "tenant_id, punch_time"),
        @Index(name = "idx_punch_logs_employee", columnList = "employee_id, punch_time"),
        @Index(name = "idx_punch_logs_biometric", columnList = "biometric_id"),
        @Index(name = "idx_punch_logs_sha256", columnList = "sha256"),  // NEW
        @Index(name = "idx_punch_logs_source", columnList = "punch_source, created_at")  // NEW
    }
)
```

**Add validation method** (add at end of class, before closing brace):

```java
/**
 * Validate SHA256 field based on punch source.
 * HR_MANUAL entries can have NULL sha256; all others must have it.
 */
@PrePersist
@PreUpdate
private void validateSha256() {
    // HR_MANUAL and LEGACY can have NULL sha256
    if (punchSource == PunchSource.HR_MANUAL || punchSource == PunchSource.LEGACY) {
        return;  // Validation passes
    }

    // All other sources must have SHA256
    if (sha256 == null) {
        throw new IllegalStateException(
            String.format("SHA256 is required for punch source: %s (ADR-002)", punchSource)
        );
    }

    // Validate SHA256 format (64 hex characters)
    if (!sha256.matches("^[a-f0-9]{64}$")) {
        throw new IllegalArgumentException(
            String.format("Invalid SHA256 format: %s (must be 64 lowercase hex chars)", sha256)
        );
    }
}
```

**Validation**:
```bash
# Compile
/home/sysadmin/tools/apache-maven-3.9.6/bin/mvn clean compile

# Expected: BUILD SUCCESS
```

---

## PHASE 4: Update PunchLogRepository

### Step 4.1: Add SHA256 Query Methods

**File**: `src/main/java/com/hrms/repository/PunchLogRepository.java`

**Action**: Add methods after existing methods (before closing brace)

**Add**:
```java
// ========== SHA256-Based Deduplication (ADR-002) ==========

/**
 * Check if punch with given SHA256 already exists.
 * Primary deduplication method (ADR-002).
 *
 * @param sha256 SHA256 hash of device_data
 * @return true if exists, false otherwise
 */
boolean existsBySha256(String sha256);

/**
 * Find punch by SHA256 hash.
 * Used for diagnostics and debugging.
 *
 * @param sha256 SHA256 hash
 * @return Optional of PunchLog
 */
Optional<PunchLog> findBySha256(String sha256);

/**
 * Find all punches from a specific source within time range.
 * Used for source-specific analytics.
 *
 * @param tenantId Tenant ID
 * @param source Punch source
 * @param startTime Start of range
 * @param endTime End of range
 * @return List of punches
 */
@Query("SELECT pl FROM PunchLog pl WHERE pl.tenantId = :tenantId " +
       "AND pl.punchSource = :source " +
       "AND pl.punchTime BETWEEN :startTime AND :endTime " +
       "ORDER BY pl.punchTime")
List<PunchLog> findByTenantAndSourceAndTimeRange(
        @Param("tenantId") String tenantId,
        @Param("source") PunchSource source,
        @Param("startTime") OffsetDateTime startTime,
        @Param("endTime") OffsetDateTime endTime);

/**
 * Count punches by source for monitoring.
 *
 * @param tenantId Tenant ID
 * @param source Punch source
 * @return Count
 */
@Query("SELECT COUNT(pl) FROM PunchLog pl WHERE pl.tenantId = :tenantId " +
       "AND pl.punchSource = :source")
long countByTenantAndSource(
        @Param("tenantId") String tenantId,
        @Param("source") PunchSource source);
```

**Validation**:
```bash
# Compile
/home/sysadmin/tools/apache-maven-3.9.6/bin/mvn clean compile

# Expected: BUILD SUCCESS
```

---

## PHASE 5: Create SHA256Calculator Utility

### Step 5.1: Create SHA256Calculator.java

**File**: `src/main/java/com/hrms/util/SHA256Calculator.java` (NEW)

**Action**: Create new file

**Content**:
```java
package com.hrms.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for SHA256 hash calculation.
 * Used for idempotent deduplication of punch records (ADR-002).
 *
 * @since 2.0 (ADR-002)
 */
@Slf4j
public class SHA256Calculator {

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    /**
     * Calculate SHA256 hash of input string.
     *
     * @param input String to hash (e.g., device_data CSV, API payload)
     * @return Lowercase hex string (64 characters)
     * @throws IllegalArgumentException if input is null
     * @throws RuntimeException if SHA-256 algorithm not available (should never happen in Java 8+)
     */
    public static String calculate(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null for SHA256 calculation");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // Should never happen (SHA-256 is mandatory in Java 8+)
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Convert byte array to lowercase hex string.
     *
     * @param bytes Byte array
     * @return Hex string (lowercase)
     */
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Verify if a string is a valid SHA256 hash (64 lowercase hex chars).
     *
     * @param hash String to verify
     * @return true if valid, false otherwise
     */
    public static boolean isValidSHA256(String hash) {
        return hash != null && hash.matches("^[a-f0-9]{64}$");
    }

    /**
     * Calculate SHA256 for device_data CSV format.
     * Helper method for consistent formatting.
     *
     * @param tenantId Tenant ID
     * @param companyId Company ID
     * @param locationId Location ID
     * @param deviceLogId Device log ID
     * @param userId User ID (biometric)
     * @param logDate Log date string
     * @param direction Direction (IN/OUT)
     * @param deviceId Device ID
     * @return SHA256 hash
     */
    public static String calculateForDeviceData(
            String tenantId, String companyId, String locationId,
            String deviceLogId, String userId, String logDate,
            String direction, String deviceId) {
        String deviceData = String.join(",",
                tenantId, companyId, locationId, deviceLogId,
                userId, logDate, direction, deviceId);
        return calculate(deviceData);
    }
}
```

**Validation**:
```bash
# Compile
/home/sysadmin/tools/apache-maven-3.9.6/bin/mvn clean compile

# Test manually
cat > /tmp/test_sha256.java << 'EOF'
import com.hrms.util.SHA256Calculator;

public class TestSHA256 {
    public static void main(String[] args) {
        // Known SHA256: "hello world" -> b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9
        String result = SHA256Calculator.calculate("hello world");
        System.out.println("Result: " + result);
        System.out.println("Expected: b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9");
        System.out.println("Match: " + result.equals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"));
    }
}
EOF
# Run after build completes
```

---

## PHASE 6: Refactor BiometricSyncServiceImpl

This is the most critical phase with extensive changes.

### Step 6.1: Update fetchFromSupabase() Method

**File**: `src/main/java/com/hrms/service/impl/BiometricSyncServiceImpl.java`

**Action**: Replace method at lines 228-260

**Replace**:
```java
private List<Map<String, Object>> fetchFromSupabase() {
```

**With**:
```java
/**
 * Fetch unprocessed biometric records from Supabase (incremental sync per ADR-002).
 * Only fetches records where sync_status IS NULL (not yet processed).
 *
 * @return List of unprocessed records (max 1000)
 */
private List<Map<String, Object>> fetchFromSupabase() {
    try {
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // ADR-002: Incremental sync - only unprocessed records
        String url = supabaseConfig.getBiometricDataEndpoint()
                   + "?select=*"
                   + "&sync_status=is.null"           // NEW: Only NULL status
                   + "&order=created_at.asc"          // CHANGED: FIFO processing (was .desc)
                   + "&limit=1000";                   // NEW: Batch limit

        log.debug("Fetching from Supabase: {}", url);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.GET, entity, String.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            JsonNode root = objectMapper.readTree(response.getBody());
            List<Map<String, Object>> records = new ArrayList<>();

            for (JsonNode node : root) {
                Map<String, Object> record = new HashMap<>();
                record.put("id", node.has("id") ? node.get("id").asText() : null);  // SHA256
                record.put("device_data", node.has("device_data") ? node.get("device_data").asText() : null);
                record.put("created_at", node.has("created_at") ? node.get("created_at").asText() : null);
                record.put("sync_status", node.has("sync_status") ? node.get("sync_status").asText() : null);
                records.add(record);
            }

            log.info("Fetched {} unprocessed records from Supabase (incremental sync)", records.size());
            return records;
        }

        log.warn("Unexpected response from Supabase: {}", response.getStatusCode());
        return Collections.emptyList();
    } catch (Exception e) {
        log.error("Failed to fetch from Supabase: {}", e.getMessage(), e);
        return Collections.emptyList();
    }
}
```

### Step 6.2: Add updateSupabaseStatus() Method

**Action**: Add NEW method after fetchFromSupabase()

**Add**:
```java
/**
 * Update sync status in Supabase (write-back per ADR-002).
 * Uses eventual consistency model - failures are logged but not thrown.
 *
 * @param supabaseId SHA256 ID from Supabase
 * @param status 'synced' or 'reject-duplicate'
 */
private void updateSupabaseStatus(String supabaseId, String status) {
    try {
        String url = supabaseConfig.getBiometricDataEndpoint()
                   + "?id=eq." + supabaseId;

        HttpHeaders headers = createHeaders();
        headers.set("Prefer", "return=minimal"); // Don't return response body (performance)

        Map<String, Object> body = Map.of(
            "sync_status", status,
            "synced_at", OffsetDateTime.now().toString()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url, HttpMethod.PATCH, entity, String.class
        );

        if (response.getStatusCode() == HttpStatus.OK ||
            response.getStatusCode() == HttpStatus.NO_CONTENT) {
            log.debug("Updated Supabase record {} to status: {}", supabaseId, status);
        } else {
            log.warn("Unexpected PATCH response from Supabase: {} for ID: {}",
                     response.getStatusCode(), supabaseId);
        }

    } catch (Exception e) {
        // EVENTUAL CONSISTENCY: Log warning, don't throw
        // Punch is already inserted in HRMS - sync status is secondary
        // Next sync will re-fetch → duplicate check catches it → marks 'reject-duplicate'
        log.warn("Failed to update Supabase status for {} to '{}': {}. Continuing anyway.",
                 supabaseId, status, e.getMessage());
    }
}

/**
 * Batch update Supabase status for multiple records.
 *
 * @param supabaseIds List of SHA256 IDs
 * @param status Status to set
 */
private void updateSupabaseStatusBatch(List<String> supabaseIds, String status) {
    if (supabaseIds == null || supabaseIds.isEmpty()) {
        return;
    }

    log.info("Updating {} Supabase records to status: {}", supabaseIds.size(), status);

    for (String id : supabaseIds) {
        updateSupabaseStatus(id, status);
    }

    // Note: Supabase REST API doesn't support bulk PATCH in single request
    // Future optimization: Use Supabase RPC function for bulk updates
}
```

### Step 6.3: Add processBatch() Method

**Action**: Add NEW method

**Add**:
```java
/**
 * Process a batch of PunchLogs atomically (ADR-002).
 * Handles DataIntegrityViolationException for race conditions.
 *
 * @param batch List of PunchLog entities to insert
 * @param idsToMarkSynced List to populate with successfully synced IDs
 * @param idsToMarkDuplicate List to populate with duplicate IDs
 */
@Transactional
private void processBatch(List<PunchLog> batch,
                         List<String> idsToMarkSynced,
                         List<String> idsToMarkDuplicate) {
    for (PunchLog punch : batch) {
        try {
            punchLogRepository.save(punch);
            idsToMarkSynced.add(punch.getSha256());
            log.debug("Inserted punch: employeeId={}, sha256={}", punch.getEmployee().getId(), punch.getSha256());
        } catch (DataIntegrityViolationException e) {
            // Race condition: Another thread inserted between check and save
            log.warn("Constraint violation for SHA256 {}: {}. Marking as duplicate.",
                     punch.getSha256(), e.getMessage());
            idsToMarkDuplicate.add(punch.getSha256());
        } catch (Exception e) {
            log.error("Unexpected error processing punch with SHA256 {}: {}",
                     punch.getSha256(), e.getMessage(), e);
            idsToMarkDuplicate.add(punch.getSha256());
        }
    }

    log.info("Batch processed: {} inserted, {} duplicates",
             idsToMarkSynced.size(), idsToMarkDuplicate.size());
}
```

### Step 6.4: Replace syncFromSupabase() Main Logic

**Action**: Replace entire method at lines 106-226

**Replace**:
```java
@Override
@Transactional
public BiometricSyncResult syncFromSupabase(String tenantId, Long companyId) {
    // ... existing code ...
}
```

**With**:
```java
/**
 * Sync biometric data from Supabase to HRMS (ADR-002 implementation).
 * Features:
 * - Incremental fetch (only sync_status=NULL)
 * - SHA256-based primary deduplication (O(1) lookup)
 * - Time-window secondary deduplication (for HR_MANUAL fallback)
 * - Batch processing (100 records per transaction)
 * - Bi-directional write-back (eventual consistency)
 *
 * @param tenantId Tenant ID
 * @param companyId Company ID
 * @return Sync result with counts
 */
@Override
public BiometricSyncResult syncFromSupabase(String tenantId, Long companyId) {
    log.info("Starting biometric sync for tenantId={}, companyId={}", tenantId, companyId);

    int totalFetched = 0;
    int inserted = 0;
    int skippedDuplicate = 0;
    int skippedNoEmployee = 0;
    int failed = 0;

    try {
        // STEP 1: Fetch unprocessed records from Supabase (incremental sync)
        List<Map<String, Object>> records = fetchFromSupabase();
        totalFetched = records.size();

        if (records.isEmpty()) {
            log.info("No new biometric records to sync");
            return new BiometricSyncResult(0, 0, 0, 0, 0, "No new records to sync");
        }

        log.info("Fetched {} records from Supabase", totalFetched);

        // STEP 2: Build employee lookup cache (one-time per sync)
        Map<String, Employee> employeeCache = buildEmployeeCache(tenantId);
        log.debug("Built employee cache with {} entries", employeeCache.size());

        // STEP 3: Process records in batches
        final int BATCH_SIZE = 100;
        List<PunchLog> currentBatch = new ArrayList<>();
        List<String> idsToMarkSynced = new ArrayList<>();
        List<String> idsToMarkDuplicate = new ArrayList<>();

        for (Map<String, Object> record : records) {
            try {
                String supabaseId = (String) record.get("id"); // SHA256 from Supabase
                String deviceData = (String) record.get("device_data");

                if (supabaseId == null || deviceData == null) {
                    log.warn("Missing required fields (id or device_data) in record: {}", record);
                    failed++;
                    continue;
                }

                // ADR-002: PRIMARY DEDUPLICATION - Check SHA256 first (O(1))
                if (punchLogRepository.existsBySha256(supabaseId)) {
                    log.debug("Duplicate detected via SHA256: {}", supabaseId);
                    skippedDuplicate++;
                    idsToMarkDuplicate.add(supabaseId);
                    continue;
                }

                // STEP 4: Parse device_data CSV
                String[] parts = deviceData.split(",");
                if (parts.length < 8) {
                    log.warn("Invalid device_data format (expected 8 fields): {}", deviceData);
                    failed++;
                    continue;
                }

                String biometricId = parts[4].trim(); // userId from CSV
                LocalDateTime punchDateTime = parseDateTime(parts[5].trim());
                String direction = parts[6].trim(); // IN/OUT

                if (punchDateTime == null) {
                    log.warn("Failed to parse punch time: {}", parts[5]);
                    failed++;
                    continue;
                }

                // STEP 5: Match employee by biometricId
                Employee employee = employeeCache.get(biometricId);
                if (employee == null) {
                    log.warn("No employee found for biometricId: {}", biometricId);
                    skippedNoEmployee++;
                    continue;
                }

                // STEP 6: SECONDARY DEDUPLICATION - Time window check (for HR_MANUAL fallback)
                // Note: This is redundant for SUPABASE source (already checked SHA256)
                // but kept for consistency with manual entry path
                OffsetDateTime punchTime = punchDateTime.atOffset(ZoneOffset.UTC);
                if (isDuplicatePunch(employee.getId(), punchTime, supabaseId)) {
                    log.debug("Duplicate detected via time window: employeeId={}, time={}",
                             employee.getId(), punchTime);
                    skippedDuplicate++;
                    idsToMarkDuplicate.add(supabaseId);
                    continue;
                }

                // STEP 7: Create PunchLog entity
                PunchLog punch = new PunchLog();
                punch.setTenantId(tenantId);
                punch.setEmployee(employee);
                punch.setPunchTime(punchTime);
                punch.setPunchType("IN".equalsIgnoreCase(direction) ? PunchType.CHECK_IN : PunchType.CHECK_OUT);
                punch.setPunchSource(PunchSource.SUPABASE); // ADR-002: Specific source
                punch.setSha256(supabaseId); // ADR-002: Store SHA256 from Supabase
                punch.setRawData(new ObjectMapper().writeValueAsString(record));
                punch.setCreatedBy("SYSTEM");
                punch.setUpdatedBy("SYSTEM");

                // Add to current batch
                currentBatch.add(punch);

                // STEP 8: Process batch when full
                if (currentBatch.size() >= BATCH_SIZE) {
                    processBatch(currentBatch, idsToMarkSynced, idsToMarkDuplicate);
                    inserted += idsToMarkSynced.size();
                    currentBatch.clear();
                }

            } catch (Exception e) {
                log.error("Error processing record: {}", record, e);
                failed++;
            }
        }

        // STEP 9: Process remaining records in final batch
        if (!currentBatch.isEmpty()) {
            processBatch(currentBatch, idsToMarkSynced, idsToMarkDuplicate);
            inserted += idsToMarkSynced.size();
        }

        // STEP 10: Write-back to Supabase (eventual consistency)
        updateSupabaseStatusBatch(idsToMarkSynced, "synced");
        updateSupabaseStatusBatch(idsToMarkDuplicate, "reject-duplicate");

        String message = String.format(
            "Sync completed: %d fetched, %d inserted, %d duplicate, %d no-employee, %d failed",
            totalFetched, inserted, skippedDuplicate, skippedNoEmployee, failed
        );
        log.info(message);

        return new BiometricSyncResult(
            totalFetched, inserted, skippedDuplicate, skippedNoEmployee, failed, message
        );

    } catch (Exception e) {
        String errorMsg = "Biometric sync failed: " + e.getMessage();
        log.error(errorMsg, e);
        return new BiometricSyncResult(
            totalFetched, inserted, skippedDuplicate, skippedNoEmployee, failed, errorMsg
        );
    }
}
```

### Step 6.5: Add buildEmployeeCache() Helper Method

**Action**: Add NEW method

**Add**:
```java
/**
 * Build in-memory cache of employee biometricId -> Employee.
 * Reduces DB queries from O(n) to O(1) per sync.
 *
 * @param tenantId Tenant ID
 * @return Map of biometricId to Employee
 */
private Map<String, Employee> buildEmployeeCache(String tenantId) {
    List<Employee> employees = employeeRepository.findByTenantId(tenantId);
    Map<String, Employee> cache = new HashMap<>();

    for (Employee emp : employees) {
        if (emp.getBiometricId() != null && !emp.getBiometricId().isBlank()) {
            cache.put(emp.getBiometricId().trim(), emp);
        }
    }

    return cache;
}
```

### Step 6.6: Update isDuplicatePunch() Method Signature

**Action**: Modify method to accept supabaseId parameter

**Replace**:
```java
private boolean isDuplicatePunch(Long employeeId, OffsetDateTime punchTime, String supabaseId) {
```

**With**:
```java
/**
 * Check if punch is duplicate via time window (SECONDARY deduplication).
 * Used as fallback for HR_MANUAL entries without SHA256.
 * For SUPABASE source, SHA256 check is PRIMARY (already done).
 *
 * @param employeeId Employee ID
 * @param punchTime Punch timestamp
 * @param supabaseId Supabase ID (currently unused, for future enhancements)
 * @return true if duplicate found within ±1 minute window
 */
private boolean isDuplicatePunch(Long employeeId, OffsetDateTime punchTime, String supabaseId) {
    // Existing logic unchanged - kept for backward compatibility
    List<PunchLog> existing = punchLogRepository.findByEmployeeIdAndPunchTimeBetween(
        employeeId,
        punchTime.minusMinutes(1),
        punchTime.plusMinutes(1)
    );
    return !existing.isEmpty();
}
```

---

## PHASE 7: Update application.properties

### Step 7.1: Add Configuration Properties

**File**: `src/main/resources/application.properties`

**Action**: Add at end of file

**Add**:
```properties
# ========== Biometric Sync Configuration (ADR-002) ==========
biometric.sync.batch-size=100
biometric.sync.fetch-limit=1000
biometric.sync.write-back-enabled=true
biometric.sync.write-back-timeout-ms=5000

# Note: For production, move Supabase key to environment variable
# export SUPABASE_SERVICE_KEY=<key>
# Then update: supabase.key=${SUPABASE_SERVICE_KEY}
```

---

## PHASE 8: Testing & Validation

### Step 8.1: Compile & Build

```bash
/home/sysadmin/tools/apache-maven-3.9.6/bin/mvn clean compile
```

### Step 8.2: Run Application

```bash
/home/sysadmin/tools/apache-maven-3.9.6/bin/mvn spring-boot:run
```

### Step 8.3: Test GraphQL Sync

```graphql
mutation {
  syncBiometricData(tenantId: "TENANT001", companyId: 1) {
    totalFetched
    inserted
    skippedDuplicate
    skippedNoEmployee
    failed
    message
  }
}
```

**Expected**: Successful sync with deduplication working

---

## Next Steps

1. Execute Phase 1 (Database migration)
2. Proceed sequentially through phases
3. Test after each phase
4. Commit after validation

Ready to execute?
