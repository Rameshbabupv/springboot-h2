-- Fresh Employee Template Sample Data
-- Database: hrmsdb
-- Date: 2025-11-28
-- Description: Complete sample data for Employee Template Configuration

-- ==============================================================================
-- STEP 1: Create Field Definitions (if not exist)
-- ==============================================================================

-- Personal Information Fields
INSERT INTO field_definition_master
(tenant_id, field_name, field_label, field_code, field_type, field_category, data_type,
 is_system_field, is_custom_field, is_searchable, is_required_by_default,
 help_text, placeholder_text, status, created_at, updated_at)
VALUES
-- Basic Personal Info (System Fields)
('550e8400-e29b-41d4-a716-446655440000', 'firstName', 'First Name', 'first_name', 'text', 'personal', 'string',
 true, false, true, true, 'Employee first name', 'Enter first name', 'active', NOW(), NOW()),

('550e8400-e29b-41d4-a716-446655440000', 'lastName', 'Last Name', 'last_name', 'text', 'personal', 'string',
 true, false, true, true, 'Employee last name', 'Enter last name', 'active', NOW(), NOW()),

('550e8400-e29b-41d4-a716-446655440000', 'dateOfBirth', 'Date of Birth', 'date_of_birth', 'date', 'personal', 'date',
 true, false, true, true, 'Employee date of birth', 'Select DOB', 'active', NOW(), NOW()),

('550e8400-e29b-41d4-a716-446655440000', 'gender', 'Gender', 'gender', 'dropdown', 'personal', 'string',
 true, false, true, true, 'Employee gender', 'Select gender', 'active', NOW(), NOW()),

-- Custom Field - Blood Group
('550e8400-e29b-41d4-a716-446655440000', 'bloodGroup', 'Blood Group', 'blood_group', 'dropdown', 'personal', 'string',
 false, true, true, false, 'Blood group classification', 'Select blood group', 'active', NOW(), NOW()),

-- Contact Information
('550e8400-e29b-41d4-a716-446655440000', 'emailPersonal', 'Personal Email', 'email_personal', 'email', 'contact', 'string',
 true, false, true, false, 'Personal email address', 'name@example.com', 'active', NOW(), NOW()),

('550e8400-e29b-41d4-a716-446655440000', 'phoneNumber', 'Phone Number', 'phone_number', 'tel', 'contact', 'string',
 true, false, true, true, 'Contact phone number', '+1234567890', 'active', NOW(), NOW()),

('550e8400-e29b-41d4-a716-446655440000', 'emergencyContact', 'Emergency Contact', 'emergency_contact', 'text', 'contact', 'string',
 true, false, true, false, 'Emergency contact person', 'Contact name', 'active', NOW(), NOW()),

-- Employment Information
('550e8400-e29b-41d4-a716-446655440000', 'employeeId', 'Employee ID', 'employee_id', 'text', 'employment', 'string',
 true, false, true, true, 'Unique employee identifier', 'EMP001', 'active', NOW(), NOW()),

('550e8400-e29b-41d4-a716-446655440000', 'dateOfJoining', 'Date of Joining', 'date_of_joining', 'date', 'employment', 'date',
 true, false, true, true, 'Employee joining date', 'Select date', 'active', NOW(), NOW()),

('550e8400-e29b-41d4-a716-446655440000', 'probationPeriod', 'Probation Period (months)', 'probation_period', 'number', 'employment', 'integer',
 true, false, false, false, 'Probation period in months', '3', 'active', NOW(), NOW()),

-- Statutory Compliance
('550e8400-e29b-41d4-a716-446655440000', 'panNumber', 'PAN Number', 'pan_number', 'text', 'statutory', 'string',
 true, false, true, true, 'Permanent Account Number', 'ABCDE1234F', 'active', NOW(), NOW()),

('550e8400-e29b-41d4-a716-446655440000', 'aadharNumber', 'Aadhar Number', 'aadhar_number', 'text', 'statutory', 'string',
 true, false, true, true, 'Aadhar card number', '1234 5678 9012', 'active', NOW(), NOW()),

-- Custom Field - Skills
('550e8400-e29b-41d4-a716-446655440000', 'primarySkills', 'Primary Skills', 'primary_skills', 'textarea', 'professional', 'string',
 false, true, true, false, 'List of primary technical skills', 'Java, Spring Boot, etc.', 'active', NOW(), NOW()),

-- Custom Field - Previous Experience
('550e8400-e29b-41d4-a716-446655440000', 'previousExperience', 'Previous Experience (years)', 'previous_experience', 'number', 'professional', 'decimal',
 false, true, false, false, 'Years of experience before joining', '5', 'active', NOW(), NOW()),

-- Bank Details
('550e8400-e29b-41d4-a716-446655440000', 'bankAccountNumber', 'Bank Account Number', 'bank_account_number', 'text', 'financial', 'string',
 true, false, false, false, 'Bank account number for salary', 'Account number', 'active', NOW(), NOW())

ON CONFLICT (tenant_id, field_name) DO NOTHING;

-- ==============================================================================
-- STEP 2: Create Employee Templates
-- ==============================================================================

-- Template 1: Permanent Employee Template (Default)
INSERT INTO employee_template
(tenant_id, template_name, template_code, description,
 company_id, location_id, division_id, department_id, section_id,
 designation_id, job_function_id, employment_type_id, grade_id,
 is_default, is_active, priority, version, effective_from,
 created_at, updated_at)
VALUES
('550e8400-e29b-41d4-a716-446655440000',
 'Permanent Employee Template',
 'PERM_EMP_TEMPLATE',
 'Standard template for permanent full-time employees with complete field requirements',
 NULL, NULL, NULL, NULL, NULL, NULL, NULL, '1', NULL, -- employment_type_id = 1 (Permanent)
 true, true, 10, 'v1.0', '2025-01-01',
 NOW(), NOW())
RETURNING id;

-- Template 2: Contract Employee Template
INSERT INTO employee_template
(tenant_id, template_name, template_code, description,
 company_id, location_id, division_id, department_id, section_id,
 designation_id, job_function_id, employment_type_id, grade_id,
 is_default, is_active, priority, version, effective_from,
 created_at, updated_at)
VALUES
('550e8400-e29b-41d4-a716-446655440000',
 'Contract Employee Template',
 'CONTRACT_EMP_TEMPLATE',
 'Template for contract/temporary employees with minimal required fields',
 NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2', NULL, -- employment_type_id = 2 (Contract)
 false, true, 8, 'v1.0', '2025-01-01',
 NOW(), NOW())
RETURNING id;

-- Template 3: Intern Template
INSERT INTO employee_template
(tenant_id, template_name, template_code, description,
 company_id, location_id, division_id, department_id, section_id,
 designation_id, job_function_id, employment_type_id, grade_id,
 is_default, is_active, priority, version, effective_from,
 created_at, updated_at)
VALUES
('550e8400-e29b-41d4-a716-446655440000',
 'Intern Template',
 'INTERN_TEMPLATE',
 'Simplified template for interns and trainees',
 NULL, NULL, NULL, NULL, NULL, NULL, NULL, '3', NULL, -- employment_type_id = 3 (Intern)
 false, true, 5, 'v1.0', '2025-01-01',
 NOW(), NOW())
RETURNING id;

-- ==============================================================================
-- STEP 3: Create Template Sections
-- ==============================================================================

-- Get template IDs (for reference, we'll use direct IDs after insert)
-- Assuming templates get IDs 1, 2, 3 respectively

-- Sections for Template 1: Permanent Employee Template
INSERT INTO employee_template_section
(template_id, section_name, section_code, section_description, section_order,
 section_icon, is_collapsible, is_expanded_by_default, created_at, updated_at)
VALUES
-- Personal Information Section
(1, 'Personal Information', 'personal_info', 'Basic personal details of the employee', 1,
 '👤', true, true, NOW(), NOW()),

-- Contact Information Section
(1, 'Contact Information', 'contact_info', 'Employee contact details', 2,
 '📞', true, true, NOW(), NOW()),

-- Employment Details Section
(1, 'Employment Details', 'employment_details', 'Job and employment information', 3,
 '💼', true, true, NOW(), NOW()),

-- Statutory Compliance Section
(1, 'Statutory Compliance', 'statutory_compliance', 'Legal and compliance documents', 4,
 '📋', true, false, NOW(), NOW()),

-- Professional Details Section
(1, 'Professional Details', 'professional_details', 'Skills and experience', 5,
 '🎓', true, false, NOW(), NOW()),

-- Financial Details Section
(1, 'Financial Details', 'financial_details', 'Bank and salary information', 6,
 '💰', true, false, NOW(), NOW());

-- Sections for Template 2: Contract Employee Template (Minimal)
INSERT INTO employee_template_section
(template_id, section_name, section_code, section_description, section_order,
 section_icon, is_collapsible, is_expanded_by_default, created_at, updated_at)
VALUES
(2, 'Basic Information', 'basic_info', 'Essential employee information', 1,
 '👤', true, true, NOW(), NOW()),

(2, 'Contact & Employment', 'contact_employment', 'Contact and job details', 2,
 '📞', true, true, NOW(), NOW()),

(2, 'Compliance', 'compliance', 'Mandatory compliance documents', 3,
 '📋', true, true, NOW(), NOW());

-- Sections for Template 3: Intern Template (Simplified)
INSERT INTO employee_template_section
(template_id, section_name, section_code, section_description, section_order,
 section_icon, is_collapsible, is_expanded_by_default, created_at, updated_at)
VALUES
(3, 'Personal & Contact', 'personal_contact', 'Basic personal and contact information', 1,
 '👤', true, true, NOW(), NOW()),

(3, 'Internship Details', 'internship_details', 'Internship program information', 2,
 '🎓', true, true, NOW(), NOW());

-- ==============================================================================
-- STEP 4: Map Fields to Sections (Template Field Configuration)
-- ==============================================================================

-- TEMPLATE 1: PERMANENT EMPLOYEE - Personal Information Section (section_id = 1)
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(1, 1, 1, 1, 'half', true, false, true, true, NOW(), NOW()),      -- firstName (REQUIRED)
(1, 1, 2, 2, 'half', true, false, true, true, NOW(), NOW()),      -- lastName (REQUIRED)
(1, 1, 3, 3, 'half', true, false, true, true, NOW(), NOW()),      -- dateOfBirth (REQUIRED)
(1, 1, 4, 4, 'half', true, false, true, true, NOW(), NOW()),      -- gender (REQUIRED)
(1, 1, 5, 5, 'half', true, false, true, true, NOW(), NOW());      -- bloodGroup (REQUIRED for permanent)

-- TEMPLATE 1: Contact Information Section (section_id = 2)
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(1, 2, 6, 1, 'half', false, false, true, true, NOW(), NOW()),     -- emailPersonal (Optional)
(1, 2, 7, 2, 'half', true, false, true, true, NOW(), NOW()),      -- phoneNumber (REQUIRED)
(1, 2, 8, 3, 'full', false, false, true, true, NOW(), NOW());     -- emergencyContact (Optional)

-- TEMPLATE 1: Employment Details Section (section_id = 3)
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(1, 3, 9, 1, 'half', true, false, true, true, NOW(), NOW()),      -- employeeId (REQUIRED)
(1, 3, 10, 2, 'half', true, false, true, true, NOW(), NOW()),     -- dateOfJoining (REQUIRED)
(1, 3, 11, 3, 'half', true, false, true, true, NOW(), NOW());     -- probationPeriod (REQUIRED)

-- TEMPLATE 1: Statutory Compliance Section (section_id = 4)
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(1, 4, 12, 1, 'half', true, false, true, true, NOW(), NOW()),     -- panNumber (REQUIRED)
(1, 4, 13, 2, 'half', true, false, true, true, NOW(), NOW());     -- aadharNumber (REQUIRED)

-- TEMPLATE 1: Professional Details Section (section_id = 5)
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(1, 5, 14, 1, 'full', false, false, true, true, NOW(), NOW()),    -- primarySkills (Optional)
(1, 5, 15, 2, 'half', false, false, true, true, NOW(), NOW());    -- previousExperience (Optional)

-- TEMPLATE 1: Financial Details Section (section_id = 6)
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(1, 6, 16, 1, 'full', false, false, true, true, NOW(), NOW());    -- bankAccountNumber (Optional)

-- ------------------------------------------------------------------------------
-- TEMPLATE 2: CONTRACT EMPLOYEE - Basic Information Section (section_id = 7)
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(2, 7, 1, 1, 'half', true, false, true, true, NOW(), NOW()),      -- firstName (REQUIRED)
(2, 7, 2, 2, 'half', true, false, true, true, NOW(), NOW()),      -- lastName (REQUIRED)
(2, 7, 3, 3, 'half', true, false, true, true, NOW(), NOW()),      -- dateOfBirth (REQUIRED)
(2, 7, 4, 4, 'half', true, false, true, true, NOW(), NOW()),      -- gender (REQUIRED)
(2, 7, 5, 5, 'half', false, false, true, true, NOW(), NOW());     -- bloodGroup (OPTIONAL for contract)

-- TEMPLATE 2: Contact & Employment Section (section_id = 8)
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(2, 8, 7, 1, 'half', true, false, true, true, NOW(), NOW()),      -- phoneNumber (REQUIRED)
(2, 8, 9, 2, 'half', true, false, true, true, NOW(), NOW()),      -- employeeId (REQUIRED)
(2, 8, 10, 3, 'half', true, false, true, true, NOW(), NOW());     -- dateOfJoining (REQUIRED)

-- TEMPLATE 2: Compliance Section (section_id = 9)
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(2, 9, 12, 1, 'half', true, false, true, true, NOW(), NOW()),     -- panNumber (REQUIRED)
(2, 9, 13, 2, 'half', false, false, true, true, NOW(), NOW());    -- aadharNumber (OPTIONAL for contract)

-- ------------------------------------------------------------------------------
-- TEMPLATE 3: INTERN - Personal & Contact Section (section_id = 10)
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(3, 10, 1, 1, 'half', true, false, true, true, NOW(), NOW()),     -- firstName (REQUIRED)
(3, 10, 2, 2, 'half', true, false, true, true, NOW(), NOW()),     -- lastName (REQUIRED)
(3, 10, 3, 3, 'half', false, false, true, true, NOW(), NOW()),    -- dateOfBirth (OPTIONAL)
(3, 10, 7, 4, 'half', true, false, true, true, NOW(), NOW()),     -- phoneNumber (REQUIRED)
(3, 10, 6, 5, 'half', false, false, true, true, NOW(), NOW());    -- emailPersonal (OPTIONAL)

-- TEMPLATE 3: Internship Details Section (section_id = 11)
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(3, 11, 9, 1, 'half', true, false, true, true, NOW(), NOW()),     -- employeeId (REQUIRED)
(3, 11, 10, 2, 'half', true, false, true, true, NOW(), NOW()),    -- dateOfJoining (REQUIRED)
(3, 11, 14, 3, 'full', false, false, true, true, NOW(), NOW());   -- primarySkills (OPTIONAL)

-- ==============================================================================
-- STEP 5: Verification Queries
-- ==============================================================================

-- Summary Report
SELECT
    '=== EMPLOYEE TEMPLATE CONFIGURATION SUMMARY ===' AS report_section;

SELECT
    'Templates Created' AS metric,
    COUNT(*) AS count
FROM employee_template;

SELECT
    'Sections Created' AS metric,
    COUNT(*) AS count
FROM employee_template_section;

SELECT
    'Field Definitions' AS metric,
    COUNT(*) AS count
FROM field_definition_master;

SELECT
    'Template Field Mappings' AS metric,
    COUNT(*) AS count
FROM employee_template_field;

-- Detailed Template View
SELECT
    t.id,
    t.template_name,
    t.template_code,
    t.is_default,
    t.priority,
    COUNT(DISTINCT s.id) as section_count,
    COUNT(DISTINCT f.id) as field_count
FROM employee_template t
LEFT JOIN employee_template_section s ON t.id = s.template_id
LEFT JOIN employee_template_field f ON t.id = f.template_id
GROUP BY t.id, t.template_name, t.template_code, t.is_default, t.priority
ORDER BY t.priority DESC;

-- Field Requirements by Template (Example: Blood Group Field)
SELECT
    t.template_name,
    s.section_name,
    fd.field_label,
    tf.is_required,
    tf.is_visible,
    CASE
        WHEN tf.is_required THEN 'REQUIRED ✓'
        ELSE 'OPTIONAL'
    END as field_status
FROM employee_template_field tf
JOIN employee_template t ON tf.template_id = t.id
JOIN employee_template_section s ON tf.section_id = s.id
JOIN field_definition_master fd ON tf.field_id = fd.id
WHERE fd.field_name = 'bloodGroup'
ORDER BY t.id;

SELECT '=== DATA INSERTION COMPLETED SUCCESSFULLY ===' AS status;
