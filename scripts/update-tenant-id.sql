-- Update Tenant IDs from UUID to "TENANT001"
-- Database: hrmsdb
-- Date: 2025-11-28

-- Update employee_template table
UPDATE employee_template
SET tenant_id = 'TENANT001'
WHERE tenant_id = '550e8400-e29b-41d4-a716-446655440000';

-- Update field_definition_master table
UPDATE field_definition_master
SET tenant_id = 'TENANT001'
WHERE tenant_id = '550e8400-e29b-41d4-a716-446655440000';

-- Verification
SELECT '=== TENANT ID UPDATE VERIFICATION ===' AS report;

SELECT 'employee_template' AS table_name,
       tenant_id,
       COUNT(*) AS record_count
FROM employee_template
GROUP BY tenant_id;

SELECT 'field_definition_master' AS table_name,
       tenant_id,
       COUNT(*) AS record_count
FROM field_definition_master
GROUP BY tenant_id;

-- Show updated templates
SELECT '=== UPDATED TEMPLATES ===' AS report;

SELECT id, tenant_id, template_name, template_code
FROM employee_template
ORDER BY id;

SELECT '✅ Tenant IDs updated to TENANT001' AS status;
