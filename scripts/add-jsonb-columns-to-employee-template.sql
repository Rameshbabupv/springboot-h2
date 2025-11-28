-- ================================================================
-- Migration Script: Add 6 JSONB Columns to employee_template
-- Date: 2025-11-28
-- Purpose: Add missing JSONB columns for complete criteria support
-- ================================================================

-- Add the 6 new JSONB columns to employee_template table
ALTER TABLE employee_template
ADD COLUMN IF NOT EXISTS applicable_divisions JSONB DEFAULT '[]'::jsonb,
ADD COLUMN IF NOT EXISTS applicable_departments JSONB DEFAULT '[]'::jsonb,
ADD COLUMN IF NOT EXISTS applicable_sections JSONB DEFAULT '[]'::jsonb,
ADD COLUMN IF NOT EXISTS applicable_designations JSONB DEFAULT '[]'::jsonb,
ADD COLUMN IF NOT EXISTS applicable_job_functions JSONB DEFAULT '[]'::jsonb,
ADD COLUMN IF NOT EXISTS applicable_employment_types JSONB DEFAULT '[]'::jsonb;

-- ================================================================
-- Verification Query
-- ================================================================
-- Run this query to verify all JSONB columns are present:

SELECT column_name, data_type, column_default
FROM information_schema.columns
WHERE table_name = 'employee_template'
AND column_name LIKE 'applicable%'
ORDER BY column_name;

-- Expected Result (11 columns total):
-- applicable_categories         | jsonb | '[]'::jsonb
-- applicable_companies          | jsonb | '[]'::jsonb
-- applicable_departments        | jsonb | '[]'::jsonb  ← NEW
-- applicable_designations       | jsonb | '[]'::jsonb  ← NEW
-- applicable_divisions          | jsonb | '[]'::jsonb  ← NEW
-- applicable_employment_types   | jsonb | '[]'::jsonb  ← NEW
-- applicable_grades             | jsonb | '[]'::jsonb
-- applicable_groups             | jsonb | '[]'::jsonb
-- applicable_job_functions      | jsonb | '[]'::jsonb  ← NEW
-- applicable_locations          | jsonb | '[]'::jsonb
-- applicable_sections           | jsonb | '[]'::jsonb  ← NEW

-- ================================================================
-- Test Data Query
-- ================================================================
-- Run this to check existing template data:

SELECT
    id,
    template_name,
    applicable_companies,
    applicable_locations,
    applicable_divisions,
    applicable_departments,
    applicable_sections,
    applicable_designations,
    applicable_job_functions,
    applicable_employment_types
FROM employee_template
ORDER BY id;

-- ================================================================
-- Rollback (if needed)
-- ================================================================
-- CAUTION: Only run if you need to rollback the changes!

-- ALTER TABLE employee_template
-- DROP COLUMN IF EXISTS applicable_divisions,
-- DROP COLUMN IF EXISTS applicable_departments,
-- DROP COLUMN IF EXISTS applicable_sections,
-- DROP COLUMN IF EXISTS applicable_designations,
-- DROP COLUMN IF EXISTS applicable_job_functions,
-- DROP COLUMN IF EXISTS applicable_employment_types;
