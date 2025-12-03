-- =====================================================
-- User Management Module - Database Schema
-- Tables: user_account, user_session, user_password_reset, user_activity_log
-- Created: 2025-11-29
-- =====================================================

-- Table 1: user_account
-- Main user account table with authentication and profile information
CREATE TABLE IF NOT EXISTS user_account (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    employee_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_locked BOOLEAN NOT NULL DEFAULT false,
    is_email_verified BOOLEAN NOT NULL DEFAULT false,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP NULL,
    last_login_ip VARCHAR(45) NULL,
    password_changed_at TIMESTAMP NULL,
    password_expires_at TIMESTAMP NULL,
    must_change_password BOOLEAN NOT NULL DEFAULT false,
    created_by BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    -- Foreign key constraints
    CONSTRAINT fk_user_account_employee FOREIGN KEY (employee_id)
        REFERENCES employees(id) ON DELETE RESTRICT,
    CONSTRAINT fk_user_account_created_by FOREIGN KEY (created_by)
        REFERENCES user_account(id) ON DELETE SET NULL,
    CONSTRAINT fk_user_account_updated_by FOREIGN KEY (updated_by)
        REFERENCES user_account(id) ON DELETE SET NULL,

    -- Unique constraints
    CONSTRAINT uk_user_account_employee UNIQUE (employee_id),
    CONSTRAINT uk_user_account_tenant_username UNIQUE (tenant_id, username)
);

-- Table 2: user_session
-- Session management for logged-in users
CREATE TABLE IF NOT EXISTS user_session (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_token VARCHAR(512) NOT NULL,
    refresh_token VARCHAR(512) NULL,
    device_info JSONB NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_user_session_user FOREIGN KEY (user_id)
        REFERENCES user_account(id) ON DELETE CASCADE,

    -- Unique constraints
    CONSTRAINT uk_user_session_token UNIQUE (session_token),
    CONSTRAINT uk_user_session_refresh_token UNIQUE (refresh_token)
);

-- Table 3: user_password_reset
-- Password reset token management
CREATE TABLE IF NOT EXISTS user_password_reset (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reset_token VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    ip_address VARCHAR(45) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id)
        REFERENCES user_account(id) ON DELETE CASCADE,

    -- Unique constraints
    CONSTRAINT uk_password_reset_token UNIQUE (reset_token)
);

-- Table 4: user_activity_log
-- Audit trail for all user actions
CREATE TABLE IF NOT EXISTS user_activity_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    resource_type VARCHAR(100) NULL,
    resource_id VARCHAR(100) NULL,
    description TEXT NULL,
    metadata JSONB NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_activity_log_user FOREIGN KEY (user_id)
        REFERENCES user_account(id) ON DELETE CASCADE
);

-- =====================================================
-- Indexes for Performance Optimization
-- =====================================================

-- user_account indexes
CREATE INDEX IF NOT EXISTS idx_user_account_tenant ON user_account(tenant_id);
CREATE INDEX IF NOT EXISTS idx_user_account_employee ON user_account(employee_id);
CREATE INDEX IF NOT EXISTS idx_user_account_email ON user_account(email);
CREATE INDEX IF NOT EXISTS idx_user_account_role ON user_account(role);
CREATE INDEX IF NOT EXISTS idx_user_account_active ON user_account(is_active);
CREATE INDEX IF NOT EXISTS idx_user_account_deleted ON user_account(deleted_at);

-- user_session indexes
CREATE INDEX IF NOT EXISTS idx_user_session_user ON user_session(user_id);
CREATE INDEX IF NOT EXISTS idx_user_session_token ON user_session(session_token);
CREATE INDEX IF NOT EXISTS idx_user_session_refresh ON user_session(refresh_token);
CREATE INDEX IF NOT EXISTS idx_user_session_active ON user_session(is_active, expires_at);
CREATE INDEX IF NOT EXISTS idx_user_session_expires ON user_session(expires_at);

-- user_password_reset indexes
CREATE INDEX IF NOT EXISTS idx_password_reset_user ON user_password_reset(user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_token ON user_password_reset(reset_token);
CREATE INDEX IF NOT EXISTS idx_password_reset_expires ON user_password_reset(expires_at);
CREATE INDEX IF NOT EXISTS idx_password_reset_used ON user_password_reset(used_at);

-- user_activity_log indexes
CREATE INDEX IF NOT EXISTS idx_activity_log_user ON user_activity_log(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_log_action ON user_activity_log(action_type);
CREATE INDEX IF NOT EXISTS idx_activity_log_created ON user_activity_log(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_activity_log_resource ON user_activity_log(resource_type, resource_id);

-- =====================================================
-- Comments for Documentation
-- =====================================================

COMMENT ON TABLE user_account IS 'User account table with authentication credentials and profile information';
COMMENT ON TABLE user_session IS 'Active user sessions with JWT tokens and device information';
COMMENT ON TABLE user_password_reset IS 'Password reset tokens with expiration tracking';
COMMENT ON TABLE user_activity_log IS 'Audit trail for all user actions and system events';

COMMENT ON COLUMN user_account.password_hash IS 'Bcrypt hashed password with cost factor 12';
COMMENT ON COLUMN user_account.role IS 'User role: SYSTEM_ADMIN, COMPANY_ADMIN, HR_MANAGER, PAYROLL_ADMIN, DEPARTMENT_MANAGER, ACCOUNTANT, RECRUITER, EMPLOYEE_SELF_SERVICE, MANAGER, PORTAL, CUSTOM';
COMMENT ON COLUMN user_account.is_locked IS 'Account locked due to failed login attempts or admin action';
COMMENT ON COLUMN user_account.failed_login_attempts IS 'Counter for failed login attempts, reset on successful login';
COMMENT ON COLUMN user_session.session_token IS 'JWT access token for authentication';
COMMENT ON COLUMN user_session.refresh_token IS 'JWT refresh token for session renewal';
COMMENT ON COLUMN user_password_reset.reset_token IS 'Hashed password reset token sent via email';

-- =====================================================
-- Initial Data / Default Values
-- =====================================================

-- Note: Initial system admin user should be created after first employee is added
-- Example: INSERT INTO user_account (tenant_id, employee_id, username, email, password_hash, role, must_change_password)
-- VALUES ('SYSTEM', 1, 'admin', 'admin@system.com', '$2a$12$...', 'SYSTEM_ADMIN', true);
