-- ============================================
-- HRMS User Privileges Management
-- Database Schema - PostgreSQL
-- Version: 1.0
-- ============================================

-- ============================================
-- 1. HRMS MODULES TABLE
-- Master table of all HRMS modules and features
-- ============================================
CREATE TABLE IF NOT EXISTS hrms_modules (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    module_code VARCHAR(100) NOT NULL,
    module_name VARCHAR(200) NOT NULL,
    module_category VARCHAR(100),
    parent_module_code VARCHAR(100),
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_modules_tenant_code UNIQUE (tenant_id, module_code)
);

-- Indexes for hrms_modules
CREATE INDEX IF NOT EXISTS idx_modules_tenant ON hrms_modules(tenant_id, is_active);
CREATE INDEX IF NOT EXISTS idx_modules_category ON hrms_modules(module_category);
CREATE INDEX IF NOT EXISTS idx_modules_parent ON hrms_modules(parent_module_code);

-- ============================================
-- 2. USER PRIVILEGES TABLE
-- Stores module-level permissions for individual users
-- ============================================
CREATE TABLE IF NOT EXISTS user_privileges (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    module_code VARCHAR(100) NOT NULL,
    can_view BOOLEAN DEFAULT FALSE,
    can_add BOOLEAN DEFAULT FALSE,
    can_edit BOOLEAN DEFAULT FALSE,
    can_delete BOOLEAN DEFAULT FALSE,
    can_approve BOOLEAN DEFAULT FALSE,
    can_backdate BOOLEAN DEFAULT FALSE,
    backdate_days INTEGER DEFAULT 0,
    menu_overrides JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_user_privileges_tenant_user_module UNIQUE (tenant_id, user_id, module_code),
    CONSTRAINT fk_user_privileges_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE
);

-- Indexes for user_privileges
CREATE INDEX IF NOT EXISTS idx_user_privileges_tenant ON user_privileges(tenant_id);
CREATE INDEX IF NOT EXISTS idx_user_privileges_user ON user_privileges(user_id);
CREATE INDEX IF NOT EXISTS idx_user_privileges_module ON user_privileges(module_code);
CREATE INDEX IF NOT EXISTS idx_user_privileges_tenant_user ON user_privileges(tenant_id, user_id);

-- ============================================
-- 3. USER ORGANIZATIONAL SCOPE TABLE
-- Defines organizational boundaries for users
-- ============================================
CREATE TABLE IF NOT EXISTS user_organizational_scope (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    scope_type VARCHAR(50) NOT NULL,
    scope_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    CONSTRAINT uk_user_org_scope_tenant_user_type_id UNIQUE (tenant_id, user_id, scope_type, scope_id),
    CONSTRAINT fk_user_org_scope_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE
);

-- Indexes for user_organizational_scope
CREATE INDEX IF NOT EXISTS idx_user_org_scope_tenant ON user_organizational_scope(tenant_id);
CREATE INDEX IF NOT EXISTS idx_user_org_scope_user ON user_organizational_scope(user_id);
CREATE INDEX IF NOT EXISTS idx_user_org_scope_type ON user_organizational_scope(scope_type);
CREATE INDEX IF NOT EXISTS idx_user_org_scope_tenant_user ON user_organizational_scope(tenant_id, user_id);

-- ============================================
-- 4. DESIGNATION PRIVILEGES TABLE
-- Template permissions at designation level
-- ============================================
CREATE TABLE IF NOT EXISTS designation_privileges (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    designation_id BIGINT NOT NULL,
    module_code VARCHAR(100) NOT NULL,
    can_view BOOLEAN DEFAULT FALSE,
    can_add BOOLEAN DEFAULT FALSE,
    can_edit BOOLEAN DEFAULT FALSE,
    can_delete BOOLEAN DEFAULT FALSE,
    can_approve BOOLEAN DEFAULT FALSE,
    can_backdate BOOLEAN DEFAULT FALSE,
    backdate_days INTEGER DEFAULT 0,
    menu_overrides JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_designation_privileges_tenant_desig_module UNIQUE (tenant_id, designation_id, module_code)
);

-- Indexes for designation_privileges
CREATE INDEX IF NOT EXISTS idx_designation_privileges_tenant ON designation_privileges(tenant_id);
CREATE INDEX IF NOT EXISTS idx_designation_privileges_designation ON designation_privileges(designation_id);
CREATE INDEX IF NOT EXISTS idx_designation_privileges_module ON designation_privileges(module_code);

-- ============================================
-- 5. PRIVILEGE AUDIT LOG TABLE
-- Complete audit trail of all privilege changes
-- ============================================
CREATE TABLE IF NOT EXISTS privilege_audit_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    user_id BIGINT,
    designation_id BIGINT,
    action VARCHAR(50) NOT NULL,
    module_code VARCHAR(100),
    changes JSONB,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500)
);

-- Indexes for privilege_audit_log
CREATE INDEX IF NOT EXISTS idx_privilege_audit_tenant ON privilege_audit_log(tenant_id);
CREATE INDEX IF NOT EXISTS idx_privilege_audit_user ON privilege_audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_privilege_audit_designation ON privilege_audit_log(designation_id);
CREATE INDEX IF NOT EXISTS idx_privilege_audit_changed_at ON privilege_audit_log(changed_at DESC);
CREATE INDEX IF NOT EXISTS idx_privilege_audit_tenant_user ON privilege_audit_log(tenant_id, user_id);

-- ============================================
-- 6. USER PRIVILEGE SETTINGS TABLE
-- Stores inheritance and override settings
-- ============================================
CREATE TABLE IF NOT EXISTS user_privilege_settings (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    inherit_from_designation BOOLEAN DEFAULT TRUE,
    user_type VARCHAR(50) DEFAULT 'REGULAR',
    is_super_admin BOOLEAN DEFAULT FALSE,
    custom_settings JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_privilege_settings_tenant_user UNIQUE (tenant_id, user_id),
    CONSTRAINT fk_user_privilege_settings_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE
);

-- Indexes for user_privilege_settings
CREATE INDEX IF NOT EXISTS idx_user_privilege_settings_tenant ON user_privilege_settings(tenant_id);
CREATE INDEX IF NOT EXISTS idx_user_privilege_settings_user ON user_privilege_settings(user_id);
CREATE INDEX IF NOT EXISTS idx_user_privilege_settings_type ON user_privilege_settings(user_type);

-- ============================================
-- SAMPLE DATA - HRMS Modules
-- ============================================
INSERT INTO hrms_modules (tenant_id, module_code, module_name, module_category, parent_module_code, display_order, is_active) VALUES
-- Employee Management
('SYSTEM', 'EMPLOYEE_MANAGEMENT', 'Employee Management', 'HR', NULL, 1, TRUE),
('SYSTEM', 'EMPLOYEE_CREATION', 'Employee Creation', 'HR', 'EMPLOYEE_MANAGEMENT', 11, TRUE),
('SYSTEM', 'EMPLOYEE_REGISTER', 'Employee Register', 'HR', 'EMPLOYEE_MANAGEMENT', 12, TRUE),
('SYSTEM', 'EMPLOYEE_PROFILE', 'Employee Profile', 'HR', 'EMPLOYEE_MANAGEMENT', 13, TRUE),

-- Attendance Management
('SYSTEM', 'ATTENDANCE_MANAGEMENT', 'Attendance Management', 'Attendance', NULL, 2, TRUE),
('SYSTEM', 'DAILY_ATTENDANCE', 'Daily Attendance', 'Attendance', 'ATTENDANCE_MANAGEMENT', 21, TRUE),
('SYSTEM', 'ATTENDANCE_REGISTER', 'Attendance Register', 'Attendance', 'ATTENDANCE_MANAGEMENT', 22, TRUE),
('SYSTEM', 'SHIFT_ROSTER', 'Shift Roster', 'Attendance', 'ATTENDANCE_MANAGEMENT', 23, TRUE),

-- Leave Management
('SYSTEM', 'LEAVE_MANAGEMENT', 'Leave Management', 'Leave', NULL, 3, TRUE),
('SYSTEM', 'LEAVE_APPLICATION', 'Leave Application', 'Leave', 'LEAVE_MANAGEMENT', 31, TRUE),
('SYSTEM', 'LEAVE_APPROVAL', 'Leave Approval', 'Leave', 'LEAVE_MANAGEMENT', 32, TRUE),
('SYSTEM', 'LEAVE_BALANCE', 'Leave Balance', 'Leave', 'LEAVE_MANAGEMENT', 33, TRUE),

-- Payroll Management
('SYSTEM', 'PAYROLL_MANAGEMENT', 'Payroll Management', 'Payroll', NULL, 4, TRUE),
('SYSTEM', 'SALARY_PROCESSING', 'Salary Processing', 'Payroll', 'PAYROLL_MANAGEMENT', 41, TRUE),
('SYSTEM', 'PAYSLIP_GENERATION', 'Payslip Generation', 'Payroll', 'PAYROLL_MANAGEMENT', 42, TRUE),
('SYSTEM', 'SALARY_REGISTER', 'Salary Register', 'Payroll', 'PAYROLL_MANAGEMENT', 43, TRUE),

-- User Management
('SYSTEM', 'USER_MANAGEMENT', 'User Management', 'Administration', NULL, 5, TRUE),
('SYSTEM', 'USER_ACCOUNTS', 'User Accounts', 'Administration', 'USER_MANAGEMENT', 51, TRUE),
('SYSTEM', 'USER_PRIVILEGES', 'User Privileges', 'Administration', 'USER_MANAGEMENT', 52, TRUE),
('SYSTEM', 'ROLE_MANAGEMENT', 'Role Management', 'Administration', 'USER_MANAGEMENT', 53, TRUE),

-- Reports
('SYSTEM', 'REPORTS', 'Reports', 'Reports', NULL, 6, TRUE),
('SYSTEM', 'EMPLOYEE_REPORTS', 'Employee Reports', 'Reports', 'REPORTS', 61, TRUE),
('SYSTEM', 'ATTENDANCE_REPORTS', 'Attendance Reports', 'Reports', 'REPORTS', 62, TRUE),
('SYSTEM', 'PAYROLL_REPORTS', 'Payroll Reports', 'Reports', 'REPORTS', 63, TRUE),

-- Masters
('SYSTEM', 'MASTERS', 'Masters', 'Configuration', NULL, 7, TRUE),
('SYSTEM', 'COMPANY_MASTER', 'Company Master', 'Configuration', 'MASTERS', 71, TRUE),
('SYSTEM', 'LOCATION_MASTER', 'Location Master', 'Configuration', 'MASTERS', 72, TRUE),
('SYSTEM', 'DEPARTMENT_MASTER', 'Department Master', 'Configuration', 'MASTERS', 73, TRUE),
('SYSTEM', 'DESIGNATION_MASTER', 'Designation Master', 'Configuration', 'MASTERS', 74, TRUE)
ON CONFLICT (tenant_id, module_code) DO NOTHING;

-- ============================================
-- COMMENTS
-- ============================================
COMMENT ON TABLE hrms_modules IS 'Master table of all HRMS modules and features';
COMMENT ON TABLE user_privileges IS 'Module-level permissions for individual users';
COMMENT ON TABLE user_organizational_scope IS 'Organizational boundaries that define what data users can access';
COMMENT ON TABLE designation_privileges IS 'Template permissions at designation level that users can inherit';
COMMENT ON TABLE privilege_audit_log IS 'Complete audit trail of all privilege changes';
COMMENT ON TABLE user_privilege_settings IS 'User-level settings for privilege inheritance and overrides';

COMMENT ON COLUMN user_organizational_scope.scope_type IS 'Values: COMPANY, LOCATION, DIVISION, DEPARTMENT, SECTION, DESIGNATION, GRADE, EMPLOYMENT_TYPE, JOB_FUNCTION';
COMMENT ON COLUMN privilege_audit_log.action IS 'Values: GRANTED, REVOKED, MODIFIED, INHERITED, OVERRIDDEN';
COMMENT ON COLUMN user_privilege_settings.user_type IS 'Values: REGULAR, POWER_USER, ADMIN, SUPER_ADMIN';
