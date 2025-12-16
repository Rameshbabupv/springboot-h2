-- ============================================================
-- Migration: V003__add_keycloak_integration.sql
-- Description: Add Keycloak authentication integration tables and columns
-- Author: Backend Team
-- Date: 2025-12-16
-- References: Keycloak Authentication Integration Spec
-- ============================================================

-- ============================================================
-- STEP 1: Add keycloak_user_id column to user_accounts table
-- ============================================================

ALTER TABLE user_accounts ADD COLUMN keycloak_user_id VARCHAR(255);

-- Add unique constraint to ensure one-to-one mapping between user_accounts and Keycloak users
ALTER TABLE user_accounts ADD CONSTRAINT uk_user_keycloak_id UNIQUE (keycloak_user_id);

-- Add index for fast lookup by Keycloak ID
CREATE INDEX idx_user_keycloak_id ON user_accounts(keycloak_user_id);

-- Add comment for documentation
COMMENT ON COLUMN user_accounts.keycloak_user_id IS 'Unique identifier from Keycloak realm, used for linking backend users to Keycloak users';

-- ============================================================
-- STEP 2: Create user_company_access junction table
-- ============================================================

-- This table manages the many-to-many relationship between users and companies
-- Allows users to access multiple companies within a tenant
-- Database is the source of truth for company access permissions

CREATE TABLE user_company_access (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    granted_by BIGINT,
    granted_at TIMESTAMP NOT NULL DEFAULT NOW(),

    -- Foreign key constraints
    CONSTRAINT fk_uca_user FOREIGN KEY (user_id)
        REFERENCES user_accounts(id) ON DELETE CASCADE,
    CONSTRAINT fk_uca_company FOREIGN KEY (company_id)
        REFERENCES company(id) ON DELETE CASCADE,
    CONSTRAINT fk_uca_granted_by FOREIGN KEY (granted_by)
        REFERENCES user_accounts(id) ON DELETE SET NULL,

    -- Unique constraint to prevent duplicate company assignments
    CONSTRAINT uk_user_company UNIQUE (user_id, company_id)
);

-- ============================================================
-- STEP 3: Create indexes for optimal query performance
-- ============================================================

-- Index for finding all companies a user has access to
CREATE INDEX idx_uca_user_id ON user_company_access(user_id);

-- Index for finding all users with access to a company
CREATE INDEX idx_uca_company_id ON user_company_access(company_id);

-- Index for tenant-based queries
CREATE INDEX idx_uca_tenant_id ON user_company_access(tenant_id);

-- Composite index for common query: user + tenant filter
CREATE INDEX idx_uca_user_tenant ON user_company_access(user_id, tenant_id);

-- Composite index for active company access queries
CREATE INDEX idx_uca_active_user ON user_company_access(user_id, is_active);

-- ============================================================
-- STEP 4: Add table and column comments for documentation
-- ============================================================

COMMENT ON TABLE user_company_access IS 'Junction table managing many-to-many relationship between users and companies. Enables multi-company access within tenants.';

COMMENT ON COLUMN user_company_access.id IS 'Primary key, auto-generated identifier';
COMMENT ON COLUMN user_company_access.user_id IS 'Reference to user_accounts table';
COMMENT ON COLUMN user_company_access.company_id IS 'Reference to company table';
COMMENT ON COLUMN user_company_access.tenant_id IS 'Tenant identifier for data isolation and filtering';
COMMENT ON COLUMN user_company_access.is_active IS 'Flag to soft-delete access without losing audit trail';
COMMENT ON COLUMN user_company_access.granted_by IS 'Reference to user_accounts, tracks who granted this access (typically app_admin)';
COMMENT ON COLUMN user_company_access.granted_at IS 'Timestamp when access was granted';

-- ============================================================
-- STEP 5: Seed initial data from existing company assignments
-- ============================================================

-- For existing users who have company_id in their user_accounts record,
-- create corresponding entries in user_company_access
-- This maintains backward compatibility with existing data

INSERT INTO user_company_access (user_id, company_id, tenant_id, is_active, granted_at)
SELECT
    ua.id,
    c.id,
    c.tenant_id,
    true,
    COALESCE(ua.updated_at, ua.created_at, NOW())
FROM user_accounts ua
JOIN company c ON c.id = ua.company_id
ON CONFLICT (user_id, company_id) DO NOTHING;

-- ============================================================
-- Verification Queries (for manual validation after migration)
-- ============================================================

-- Verify keycloak_user_id column exists
-- SELECT column_name, data_type, is_nullable
-- FROM information_schema.columns
-- WHERE table_name = 'user_accounts' AND column_name = 'keycloak_user_id';

-- Verify user_company_access table exists
-- SELECT table_name FROM information_schema.tables
-- WHERE table_schema = 'public' AND table_name = 'user_company_access';

-- Verify indexes were created
-- SELECT schemaname, tablename, indexname FROM pg_indexes
-- WHERE tablename = 'user_company_access';

-- Verify initial data migration
-- SELECT COUNT(*) as user_company_access_count FROM user_company_access;

-- Verify no duplicate entries
-- SELECT user_id, company_id, COUNT(*) as count
-- FROM user_company_access
-- GROUP BY user_id, company_id
-- HAVING COUNT(*) > 1;

-- ============================================================
-- Rollback Script (if needed)
-- ============================================================

-- DROP TABLE IF EXISTS user_company_access CASCADE;
-- DROP INDEX IF EXISTS idx_user_keycloak_id;
-- ALTER TABLE user_accounts DROP CONSTRAINT IF EXISTS uk_user_keycloak_id;
-- ALTER TABLE user_accounts DROP COLUMN IF EXISTS keycloak_user_id;

-- ============================================================
-- Migration Complete
-- ============================================================
