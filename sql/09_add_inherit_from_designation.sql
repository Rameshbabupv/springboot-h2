-- =====================================================
-- Migration: Add inheritFromDesignation to user_account
-- Date: 2025-12-01
-- Description: Add column to control whether user inherits
--              permissions from their designation template
-- =====================================================

-- Add the column (nullable initially for existing rows)
ALTER TABLE user_account
ADD COLUMN IF NOT EXISTS inherit_from_designation BOOLEAN;

-- Add comment
COMMENT ON COLUMN user_account.inherit_from_designation IS
'When true, user inherits permissions from their designation template. When false, user has custom permissions.';

-- Set default value for existing rows
UPDATE user_account
SET inherit_from_designation = TRUE
WHERE inherit_from_designation IS NULL;

-- Now make it NOT NULL with default
ALTER TABLE user_account
ALTER COLUMN inherit_from_designation SET DEFAULT TRUE;

ALTER TABLE user_account
ALTER COLUMN inherit_from_designation SET NOT NULL;

-- Verify the change
SELECT
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'user_account'
  AND column_name = 'inherit_from_designation';

-- Display current values
SELECT
    id,
    username,
    role,
    inherit_from_designation
FROM user_account
WHERE deleted_at IS NULL
ORDER BY id;

-- Summary
DO $$
DECLARE
    inherit_true_count INTEGER;
    inherit_false_count INTEGER;
    total_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO inherit_true_count
    FROM user_account
    WHERE inherit_from_designation = TRUE AND deleted_at IS NULL;

    SELECT COUNT(*) INTO inherit_false_count
    FROM user_account
    WHERE inherit_from_designation = FALSE AND deleted_at IS NULL;

    total_count := inherit_true_count + inherit_false_count;

    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Inherit From Designation Migration';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Inherit = TRUE:  % users', inherit_true_count;
    RAISE NOTICE 'Inherit = FALSE: % users', inherit_false_count;
    RAISE NOTICE '----------------------------------------';
    RAISE NOTICE 'TOTAL:          % active users', total_count;
    RAISE NOTICE '========================================';
    RAISE NOTICE '';
END $$;
