-- Migration: V005__add_payhead_master_fields.sql
-- Date: December 17, 2025
-- Purpose: Add missing columns to payhead_master table for calculation rules and tax treatment
-- Database: PostgreSQL

-- =====================================================
-- ALTER TABLE: Add missing columns to payhead_master
-- =====================================================
-- Add rounding_rule column
ALTER TABLE payhead_master
ADD COLUMN IF NOT EXISTS rounding_rule VARCHAR(50) DEFAULT 'NONE'
COMMENT 'NONE, ROUND, FLOOR, CEIL, ROUND_10, ROUND_100';

-- Add min_value column
ALTER TABLE payhead_master
ADD COLUMN IF NOT EXISTS min_value DECIMAL(10, 2)
COMMENT 'Minimum value for this payhead';

-- Add max_value column
ALTER TABLE payhead_master
ADD COLUMN IF NOT EXISTS max_value DECIMAL(10, 2)
COMMENT 'Maximum value for this payhead';

-- Add applicable_condition column
ALTER TABLE payhead_master
ADD COLUMN IF NOT EXISTS applicable_condition VARCHAR(500)
COMMENT 'Condition for applicability e.g., "GROSS <= 21000"';

-- Add description column
ALTER TABLE payhead_master
ADD COLUMN IF NOT EXISTS description TEXT;

-- Add affects_pt column (Professional Tax)
ALTER TABLE payhead_master
ADD COLUMN IF NOT EXISTS affects_pt BOOLEAN DEFAULT FALSE
COMMENT 'Include in Professional Tax calculation';

-- =====================================================
-- ADD INDEXES for new columns
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_payhead_rounding_rule
ON payhead_master(rounding_rule);

CREATE INDEX IF NOT EXISTS idx_payhead_min_max
ON payhead_master(min_value, max_value);

-- =====================================================
-- UPDATE EXISTING PAYHEADS (if any) with default values
-- =====================================================
-- Set Professional Tax flag for PT payhead (if exists)
UPDATE payhead_master
SET affects_pt = TRUE
WHERE payhead_code = 'PT' AND payhead_type = 'DEDUCTION'
  AND affects_pt IS FALSE;

-- Set rounding rule for existing payheads that don't have it
UPDATE payhead_master
SET rounding_rule = 'ROUND'
WHERE rounding_rule IS NULL OR rounding_rule = '';

-- =====================================================
-- VERIFICATION QUERIES (can be run after migration)
-- =====================================================
-- SELECT column_name, data_type, column_default FROM information_schema.columns
-- WHERE table_name = 'payhead_master' AND column_name IN ('rounding_rule', 'min_value', 'max_value', 'applicable_condition', 'description', 'affects_pt');
-- SELECT id, payhead_code, payhead_name, rounding_rule, min_value, max_value, affects_pt FROM payhead_master LIMIT 10;
