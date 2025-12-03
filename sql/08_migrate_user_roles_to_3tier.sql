-- =====================================================
-- Migration: User Role Standardization to 3-Tier System
-- Date: 2025-12-01
-- Description: Migrate existing user roles to simplified
--              3-tier system (ADMIN, MANAGER, PORTAL)
-- =====================================================

-- Display current role distribution
SELECT role, COUNT(*) as user_count
FROM user_account
WHERE deleted_at IS NULL
GROUP BY role
ORDER BY user_count DESC;

-- Backup current roles (optional, for rollback)
-- CREATE TABLE user_account_role_backup AS
-- SELECT id, role, updated_at FROM user_account WHERE deleted_at IS NULL;

-- =====================================================
-- STEP 1: Migrate legacy roles to primary roles
-- =====================================================

-- Migrate all ADMIN-type roles to ADMIN
UPDATE user_account
SET role = 'ADMIN',
    updated_at = CURRENT_TIMESTAMP
WHERE role IN ('SYSTEM_ADMIN', 'COMPANY_ADMIN', 'ADMINISTRATOR', 'SUPER_ADMIN')
  AND deleted_at IS NULL;

-- Migrate all MANAGER-type roles to MANAGER
UPDATE user_account
SET role = 'MANAGER',
    updated_at = CURRENT_TIMESTAMP
WHERE role IN (
    'HR_MANAGER',
    'PAYROLL_ADMIN',
    'DEPARTMENT_MANAGER',
    'ACCOUNTANT',
    'RECRUITER',
    'CUSTOM',
    'TEAM_LEAD',
    'SUPERVISOR'
)
AND deleted_at IS NULL;

-- Migrate all PORTAL-type roles to PORTAL
UPDATE user_account
SET role = 'PORTAL',
    updated_at = CURRENT_TIMESTAMP
WHERE role IN ('EMPLOYEE_SELF_SERVICE', 'EMPLOYEE', 'PORTAL_USER', 'USER', 'SELF_SERVICE')
  AND deleted_at IS NULL;

-- Handle any remaining unmapped roles (set to MANAGER as default)
UPDATE user_account
SET role = 'MANAGER',
    updated_at = CURRENT_TIMESTAMP
WHERE role NOT IN ('ADMIN', 'MANAGER', 'PORTAL')
  AND deleted_at IS NULL;

-- =====================================================
-- STEP 2: Add check constraint (optional, for data integrity)
-- =====================================================

-- Drop constraint if it exists (for re-running the script)
ALTER TABLE user_account
DROP CONSTRAINT IF EXISTS chk_user_role;

-- Add check constraint to ensure only valid roles
ALTER TABLE user_account
ADD CONSTRAINT chk_user_role
CHECK (role IN ('ADMIN', 'MANAGER', 'PORTAL') OR deleted_at IS NOT NULL);

-- =====================================================
-- STEP 3: Verify migration results
-- =====================================================

-- Display new role distribution
SELECT
    role,
    COUNT(*) as user_count,
    STRING_AGG(DISTINCT username, ', ') as sample_users
FROM user_account
WHERE deleted_at IS NULL
GROUP BY role
ORDER BY user_count DESC;

-- Verify all users have valid roles
SELECT COUNT(*) as users_with_invalid_roles
FROM user_account
WHERE role NOT IN ('ADMIN', 'MANAGER', 'PORTAL')
  AND deleted_at IS NULL;

-- =====================================================
-- STEP 4: Display summary
-- =====================================================

DO $$
DECLARE
    admin_count INTEGER;
    manager_count INTEGER;
    portal_count INTEGER;
    total_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO admin_count FROM user_account WHERE role = 'ADMIN' AND deleted_at IS NULL;
    SELECT COUNT(*) INTO manager_count FROM user_account WHERE role = 'MANAGER' AND deleted_at IS NULL;
    SELECT COUNT(*) INTO portal_count FROM user_account WHERE role = 'PORTAL' AND deleted_at IS NULL;

    total_count := admin_count + manager_count + portal_count;

    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'User Role Migration Summary';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'ADMIN:   % users (%.1f%%)', admin_count, (admin_count::FLOAT / NULLIF(total_count, 0) * 100);
    RAISE NOTICE 'MANAGER: % users (%.1f%%)', manager_count, (manager_count::FLOAT / NULLIF(total_count, 0) * 100);
    RAISE NOTICE 'PORTAL:  % users (%.1f%%)', portal_count, (portal_count::FLOAT / NULLIF(total_count, 0) * 100);
    RAISE NOTICE '----------------------------------------';
    RAISE NOTICE 'TOTAL:   % active users', total_count;
    RAISE NOTICE '========================================';
    RAISE NOTICE '';
END $$;

-- =====================================================
-- NOTES:
-- =====================================================
-- 1. This migration is IDEMPOTENT - safe to run multiple times
-- 2. Only affects non-deleted users (deleted_at IS NULL)
-- 3. Check constraint ensures data integrity going forward
-- 4. Backup query is commented out - uncomment if needed
-- 5. All timestamps are updated to track the migration
--
-- Role Mappings:
-- - ADMIN: Full administrative access (3 org filters)
-- - MANAGER: Department/team management (9 org filters)
-- - PORTAL: Employee self-service only
-- =====================================================
