-- Fix and Complete Field Mappings
-- Using correct field IDs from field_definition_master
-- Database: hrmsdb
-- Date: 2025-11-28

-- First, let's add the missing gender field (id=20)
INSERT INTO field_definition_master
(tenant_id, field_name, field_label, field_code, field_type, field_category, data_type,
 is_system_field, is_custom_field, is_searchable, is_required_by_default,
 help_text, placeholder_text, status, created_at, updated_at)
VALUES
('550e8400-e29b-41d4-a716-446655440000', 'genderType', 'Gender', 'gender_type', 'dropdown', 'personal', 'string',
 true, false, true, true, 'Employee gender', 'Select gender', 'active', NOW(), NOW())
ON CONFLICT (tenant_id, field_name) DO NOTHING
RETURNING id;

-- Now add the missing field mappings for Personal Information sections

-- TEMPLATE 8 - Section 12: Personal Information (was missing due to gender field error)
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(8, 12, 17, 1, 'half', true, false, true, true, NOW(), NOW()),      -- firstName (REQUIRED)
(8, 12, 18, 2, 'half', true, false, true, true, NOW(), NOW()),      -- lastName (REQUIRED)
(8, 12, 19, 3, 'half', true, false, true, true, NOW(), NOW()),      -- dateOfBirth (REQUIRED)
(8, 12, 4, 4, 'half', true, false, true, true, NOW(), NOW()),       -- gender (field_id=4, REQUIRED)
(8, 12, 21, 5, 'half', true, false, true, true, NOW(), NOW())       -- bloodGroup (REQUIRED for permanent) ⭐
ON CONFLICT (template_id, section_id, field_id) DO NOTHING;

-- TEMPLATE 9 - Section 18: Basic Information (was missing due to gender field error)
INSERT INTO employee_template_field
(template_id, section_id, field_id, display_order, display_width,
 is_required, is_readonly, is_visible, is_editable, created_at, updated_at)
VALUES
(9, 18, 17, 1, 'half', true, false, true, true, NOW(), NOW()),      -- firstName (REQUIRED)
(9, 18, 18, 2, 'half', true, false, true, true, NOW(), NOW()),      -- lastName (REQUIRED)
(9, 18, 19, 3, 'half', true, false, true, true, NOW(), NOW()),      -- dateOfBirth (REQUIRED)
(9, 18, 4, 4, 'half', true, false, true, true, NOW(), NOW()),       -- gender (field_id=4, REQUIRED)
(9, 18, 21, 5, 'half', false, false, true, true, NOW(), NOW())      -- bloodGroup (OPTIONAL for contract) ⭐
ON CONFLICT (template_id, section_id, field_id) DO NOTHING;

-- ==============================================================================
-- VERIFICATION QUERIES
-- ==============================================================================

SELECT '=== UPDATED FIELD MAPPING SUMMARY ===' AS report;

SELECT
    'Total Template Field Mappings' AS metric,
    COUNT(*) AS count
FROM employee_template_field;

-- Field count per template per section
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

-- ⭐ THE KEY DEMONSTRATION: Blood Group Field (REQUIRED vs OPTIONAL)
SELECT '=== ⭐ BLOOD GROUP FIELD: REQUIRED vs OPTIONAL DEMONSTRATION ===' AS report;

SELECT
    t.template_name,
    s.section_name,
    fd.field_label,
    fd.is_custom_field,
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

-- Summary by template
SELECT '=== TEMPLATE SUMMARY ===' AS report;

SELECT
    t.id,
    t.template_name,
    t.template_code,
    t.is_default,
    t.priority,
    COUNT(DISTINCT s.id) as sections,
    COUNT(DISTINCT f.id) as total_fields,
    SUM(CASE WHEN f.is_required THEN 1 ELSE 0 END) as required_fields,
    SUM(CASE WHEN NOT f.is_required THEN 1 ELSE 0 END) as optional_fields
FROM employee_template t
LEFT JOIN employee_template_section s ON t.id = s.template_id
LEFT JOIN employee_template_field f ON s.id = f.section_id
GROUP BY t.id, t.template_name, t.template_code, t.is_default, t.priority
ORDER BY t.priority DESC;

SELECT '=== ✅ FRESH TEMPLATE DATA READY ===' AS status;
