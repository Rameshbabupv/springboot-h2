-- Map Fields to Template Sections
-- Template IDs: 8 (Permanent), 9 (Contract), 10 (Intern)
-- Section IDs: 12-22 (as verified)
-- Database: hrmsdb
-- Date: 2025-11-28

-- ==============================================================================
-- TEMPLATE 8: PERMANENT EMPLOYEE TEMPLATE
-- ==============================================================================

-- Section 12: Personal Information
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(8, 12, 17, 1, 'half', true, false, true, true, NOW(), NOW()),      -- firstName (REQUIRED)
(8, 12, 18, 2, 'half', true, false, true, true, NOW(), NOW()),      -- lastName (REQUIRED)
(8, 12, 19, 3, 'half', true, false, true, true, NOW(), NOW()),      -- dateOfBirth (REQUIRED)
(8, 12, 20, 4, 'half', true, false, true, true, NOW(), NOW()),      -- gender (REQUIRED)
(8, 12, 21, 5, 'half', true, false, true, true, NOW(), NOW());      -- bloodGroup (REQUIRED for permanent)

-- Section 13: Contact Information
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(8, 13, 22, 1, 'half', false, false, true, true, NOW(), NOW()),     -- emailPersonal (Optional)
(8, 13, 23, 2, 'half', true, false, true, true, NOW(), NOW()),      -- phoneNumber (REQUIRED)
(8, 13, 24, 3, 'full', false, false, true, true, NOW(), NOW());     -- emergencyContact (Optional)

-- Section 14: Employment Details
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(8, 14, 25, 1, 'half', true, false, true, true, NOW(), NOW()),      -- employeeId (REQUIRED)
(8, 14, 26, 2, 'half', true, false, true, true, NOW(), NOW()),      -- dateOfJoining (REQUIRED)
(8, 14, 27, 3, 'half', true, false, true, true, NOW(), NOW());      -- probationPeriod (REQUIRED)

-- Section 15: Statutory Compliance
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(8, 15, 28, 1, 'half', true, false, true, true, NOW(), NOW()),      -- panNumber (REQUIRED)
(8, 15, 29, 2, 'half', true, false, true, true, NOW(), NOW());      -- aadharNumber (REQUIRED)

-- Section 16: Professional Details
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(8, 16, 30, 1, 'full', false, false, true, true, NOW(), NOW()),     -- primarySkills (Optional)
(8, 16, 31, 2, 'half', false, false, true, true, NOW(), NOW());     -- previousExperience (Optional)

-- Section 17: Financial Details
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(8, 17, 32, 1, 'full', false, false, true, true, NOW(), NOW());     -- bankAccountNumber (Optional)

-- ==============================================================================
-- TEMPLATE 9: CONTRACT EMPLOYEE TEMPLATE
-- ==============================================================================

-- Section 18: Basic Information
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(9, 18, 17, 1, 'half', true, false, true, true, NOW(), NOW()),      -- firstName (REQUIRED)
(9, 18, 18, 2, 'half', true, false, true, true, NOW(), NOW()),      -- lastName (REQUIRED)
(9, 18, 19, 3, 'half', true, false, true, true, NOW(), NOW()),      -- dateOfBirth (REQUIRED)
(9, 18, 20, 4, 'half', true, false, true, true, NOW(), NOW()),      -- gender (REQUIRED)
(9, 18, 21, 5, 'half', false, false, true, true, NOW(), NOW());     -- bloodGroup (OPTIONAL for contract) ⭐

-- Section 19: Contact & Employment
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(9, 19, 23, 1, 'half', true, false, true, true, NOW(), NOW()),      -- phoneNumber (REQUIRED)
(9, 19, 25, 2, 'half', true, false, true, true, NOW(), NOW()),      -- employeeId (REQUIRED)
(9, 19, 26, 3, 'half', true, false, true, true, NOW(), NOW());      -- dateOfJoining (REQUIRED)

-- Section 20: Compliance
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(9, 20, 28, 1, 'half', true, false, true, true, NOW(), NOW()),      -- panNumber (REQUIRED)
(9, 20, 29, 2, 'half', false, false, true, true, NOW(), NOW());     -- aadharNumber (OPTIONAL for contract)

-- ==============================================================================
-- TEMPLATE 10: INTERN TEMPLATE
-- ==============================================================================

-- Section 21: Personal & Contact
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(10, 21, 17, 1, 'half', true, false, true, true, NOW(), NOW()),     -- firstName (REQUIRED)
(10, 21, 18, 2, 'half', true, false, true, true, NOW(), NOW()),     -- lastName (REQUIRED)
(10, 21, 19, 3, 'half', false, false, true, true, NOW(), NOW()),    -- dateOfBirth (OPTIONAL)
(10, 21, 23, 4, 'half', true, false, true, true, NOW(), NOW()),     -- phoneNumber (REQUIRED)
(10, 21, 22, 5, 'half', false, false, true, true, NOW(), NOW());    -- emailPersonal (OPTIONAL)

-- Section 22: Internship Details
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(10, 22, 25, 1, 'half', true, false, true, true, NOW(), NOW()),     -- employeeId (REQUIRED)
(10, 22, 26, 2, 'half', true, false, true, true, NOW(), NOW()),     -- dateOfJoining (REQUIRED)
(10, 22, 30, 3, 'full', false, false, true, true, NOW(), NOW());    -- primarySkills (OPTIONAL)

-- ==============================================================================
-- VERIFICATION QUERIES
-- ==============================================================================

-- Summary
SELECT '=== FIELD MAPPING SUMMARY ===' AS report;

SELECT
    'Template Field Mappings Created' AS metric,
    COUNT(*) AS count
FROM employee_template_field;

-- Detailed View
SELECT
    t.template_name,
    s.section_name,
    COUNT(f.id) as field_count,
    SUM(CASE WHEN f.is_required THEN 1 ELSE 0 END) as required_fields,
    SUM(CASE WHEN NOT f.is_required THEN 1 ELSE 0 END) as optional_fields
FROM employee_template t
JOIN employee_template_section s ON t.id = s.template_id
LEFT JOIN employee_template_field f ON s.id = f.section_id
GROUP BY t.id, t.template_name, s.id, s.section_name, s.section_order
ORDER BY t.id, s.section_order;

-- Blood Group Field Example (REQUIRED vs OPTIONAL)
SELECT
    '=== BLOOD GROUP FIELD CONFIGURATION ===' AS report;

SELECT
    t.template_name,
    s.section_name,
    fd.field_label,
    CASE
        WHEN tf.is_required THEN '✅ REQUIRED'
        ELSE '⭕ OPTIONAL'
    END as field_requirement,
    tf.display_width,
    tf.display_order
FROM employee_template_field tf
JOIN employee_template t ON tf.template_id = t.id
JOIN employee_template_section s ON tf.section_id = s.id
JOIN field_definition_master fd ON tf.field_id = fd.id
WHERE fd.field_name = 'bloodGroup'
ORDER BY t.id;

-- Complete Template Structure
SELECT
    '=== COMPLETE TEMPLATE STRUCTURE ===' AS report;

SELECT
    t.id as template_id,
    t.template_name,
    t.template_code,
    t.is_default,
    t.priority,
    s.section_order,
    s.section_name,
    f.display_order,
    fd.field_label,
    CASE WHEN f.is_required THEN 'REQ' ELSE 'OPT' END as req_status
FROM employee_template t
LEFT JOIN employee_template_section s ON t.id = s.template_id
LEFT JOIN employee_template_field f ON s.id = f.section_id
LEFT JOIN field_definition_master fd ON f.field_id = fd.id
ORDER BY t.id, s.section_order, f.display_order;

SELECT '=== DATA INSERTION COMPLETED SUCCESSFULLY ===' AS status;
