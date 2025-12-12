-- ============================================================
-- Migration: V002__add_sha256_to_punch_logs.sql
-- Description: Add SHA256 column for idempotent biometric sync (ADR-002)
-- Author: Architecture Team
-- Date: 2025-12-12
-- References: ADR-002 (SHA256-Based Idempotent Biometric Sync)
-- ============================================================

-- STEP 1: Add sha256 column (nullable initially for backfill)
ALTER TABLE punch_logs ADD COLUMN IF NOT EXISTS sha256 VARCHAR(64);

COMMENT ON COLUMN punch_logs.sha256 IS 'SHA256 hash for idempotent deduplication. NULL only for HR_MANUAL entries. Matches Supabase biometric_data.id for SUPABASE source.';

-- STEP 2: Backfill SHA256 for existing BIOMETRIC entries
-- Note: This reconstructs SHA256 from rawData if possible
-- For records without rawData, sha256 remains NULL (will be marked LEGACY)
UPDATE punch_logs
SET sha256 = (
    CASE
        WHEN raw_data::jsonb ? 'id' THEN raw_data::jsonb->>'id'
        ELSE NULL
    END
)
WHERE punch_source = 'BIOMETRIC'
  AND sha256 IS NULL
  AND raw_data IS NOT NULL;

-- STEP 3: Drop old PunchSource check constraint
-- Required to allow new enum values (SUPABASE, HR_MANUAL, USER_PORTAL, REST_API, IMPORT, MOBILE, LEGACY)
ALTER TABLE punch_logs DROP CONSTRAINT IF EXISTS punch_logs_punch_source_check;

-- STEP 3a: Update PunchSource for existing data
-- Mark all existing BIOMETRIC entries as LEGACY (pre-ADR-002 data)
UPDATE punch_logs
SET punch_source = 'LEGACY'
WHERE punch_source = 'BIOMETRIC';

-- STEP 3b: Add new PunchSource check constraint with all enum values
ALTER TABLE punch_logs
ADD CONSTRAINT punch_logs_punch_source_check
CHECK (punch_source IN (
    'SUPABASE',      -- New: Biometric devices via Supabase
    'HR_MANUAL',     -- New: HR/Admin manual entry
    'USER_PORTAL',   -- New: Employee self-service portal
    'REST_API',      -- New: External API integrations
    'IMPORT',        -- New: Bulk CSV/Excel imports
    'MOBILE',        -- Existing: Mobile app (already supported)
    'LEGACY',        -- New: Pre-ADR-002 data
    -- Deprecated values (for backward compatibility during transition)
    'BIOMETRIC',
    'MANUAL',
    'PORTAL',
    'EXCEL_IMPORT',
    'API'
));

COMMENT ON CONSTRAINT punch_logs_punch_source_check ON punch_logs IS 'ADR-002: Expanded PunchSource enum with specific source tracking';

-- STEP 4: Create partial unique index on sha256
-- Only indexes non-NULL values (allows multiple NULL for HR_MANUAL)
CREATE UNIQUE INDEX IF NOT EXISTS idx_punch_logs_sha256
ON punch_logs(sha256)
WHERE sha256 IS NOT NULL;

COMMENT ON INDEX idx_punch_logs_sha256 IS 'Partial unique index: Enforces SHA256 uniqueness for all sources except HR_MANUAL (which has NULL sha256)';

-- STEP 5: Create index for source-based queries (monitoring/analytics)
CREATE INDEX IF NOT EXISTS idx_punch_logs_source
ON punch_logs(tenant_id, punch_source);

COMMENT ON INDEX idx_punch_logs_source IS 'Index for source-specific analytics queries (e.g., count punches by source)';

-- STEP 6: Add check constraint for sha256 format validation
ALTER TABLE punch_logs
ADD CONSTRAINT chk_punch_logs_sha256_format
CHECK (sha256 IS NULL OR sha256 ~ '^[a-f0-9]{64}$');

COMMENT ON CONSTRAINT chk_punch_logs_sha256_format ON punch_logs IS 'Validates SHA256 format: 64 lowercase hex characters';

-- ============================================================
-- Verification Queries (for manual validation after migration)
-- ============================================================

-- Query 1: Check column exists
-- SELECT column_name, data_type, character_maximum_length, is_nullable
-- FROM information_schema.columns
-- WHERE table_name = 'punch_logs' AND column_name = 'sha256';

-- Query 2: Check unique index exists
-- SELECT indexname, indexdef
-- FROM pg_indexes
-- WHERE tablename = 'punch_logs' AND indexname = 'idx_punch_logs_sha256';

-- Query 3: Count records by source (should show LEGACY instead of BIOMETRIC)
-- SELECT punch_source, COUNT(*)
-- FROM punch_logs
-- GROUP BY punch_source
-- ORDER BY COUNT(*) DESC;

-- Query 4: Check sha256 NULL distribution
-- SELECT
--     punch_source,
--     COUNT(*) as total,
--     COUNT(sha256) as with_sha256,
--     COUNT(*) - COUNT(sha256) as null_sha256
-- FROM punch_logs
-- GROUP BY punch_source;

-- ============================================================
-- Rollback Script (if needed)
-- ============================================================

-- DROP INDEX IF EXISTS idx_punch_logs_sha256;
-- DROP INDEX IF EXISTS idx_punch_logs_source;
-- ALTER TABLE punch_logs DROP CONSTRAINT IF EXISTS chk_punch_logs_sha256_format;
-- ALTER TABLE punch_logs DROP COLUMN IF EXISTS sha256;
-- UPDATE punch_logs SET punch_source = 'BIOMETRIC' WHERE punch_source = 'LEGACY';

-- ============================================================
-- Migration Complete
-- ============================================================
