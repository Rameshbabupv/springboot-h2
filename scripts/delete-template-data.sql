-- Delete Employee Template Data - Clean Slate Script
-- Execute this script to remove all existing employee template data
-- Database: hrmsdb
-- Date: 2025-11-28

-- Step 1: Delete in correct order due to foreign key constraints

-- Delete template fields first (references sections and field definitions)
DELETE FROM employee_template_field;
COMMIT;

-- Delete template sections (references templates)
DELETE FROM employee_template_section;
COMMIT;

-- Delete template version history
DELETE FROM employee_template_version;
COMMIT;

-- Delete templates
DELETE FROM employee_template;
COMMIT;

-- Delete field definitions (if you want to start completely fresh)
-- UNCOMMENT THE LINES BELOW IF YOU WANT TO DELETE FIELD DEFINITIONS TOO
-- DELETE FROM field_definition_master;
-- COMMIT;

-- Verify deletion
SELECT 'employee_template' as table_name, COUNT(*) as record_count FROM employee_template
UNION ALL
SELECT 'employee_template_section', COUNT(*) FROM employee_template_section
UNION ALL
SELECT 'employee_template_field', COUNT(*) FROM employee_template_field
UNION ALL
SELECT 'employee_template_version', COUNT(*) FROM employee_template_version
UNION ALL
SELECT 'field_definition_master', COUNT(*) FROM field_definition_master;

-- Expected output: All counts should be 0 (except field_definition_master if you kept it)
