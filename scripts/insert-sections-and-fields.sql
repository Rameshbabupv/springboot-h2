-- Insert Template Sections and Fields
-- Using actual template IDs: 8, 9, 10
-- Database: hrmsdb
-- Date: 2025-11-28

-- ==============================================================================
-- STEP 1: Create Template Sections
-- ==============================================================================

-- Sections for Template 8: Permanent Employee Template
INSERT INTO employee_template_section
(template_id, section_name, section_code, section_description, section_order,
 section_icon, is_collapsible, is_expanded_by_default, created_at, updated_at)
VALUES
-- Personal Information Section
(8, 'Personal Information', 'personal_info', 'Basic personal details of the employee', 1,
 '👤', true, true, NOW(), NOW()),

-- Contact Information Section
(8, 'Contact Information', 'contact_info', 'Employee contact details', 2,
 '📞', true, true, NOW(), NOW()),

-- Employment Details Section
(8, 'Employment Details', 'employment_details', 'Job and employment information', 3,
 '💼', true, true, NOW(), NOW()),

-- Statutory Compliance Section
(8, 'Statutory Compliance', 'statutory_compliance', 'Legal and compliance documents', 4,
 '📋', true, false, NOW(), NOW()),

-- Professional Details Section
(8, 'Professional Details', 'professional_details', 'Skills and experience', 5,
 '🎓', true, false, NOW(), NOW()),

-- Financial Details Section
(8, 'Financial Details', 'financial_details', 'Bank and salary information', 6,
 '💰', true, false, NOW(), NOW());

-- Sections for Template 9: Contract Employee Template (Minimal)
INSERT INTO employee_template_section
(template_id, section_name, section_code, section_description, section_order,
 section_icon, is_collapsible, is_expanded_by_default, created_at, updated_at)
VALUES
(9, 'Basic Information', 'basic_info', 'Essential employee information', 1,
 '👤', true, true, NOW(), NOW()),

(9, 'Contact & Employment', 'contact_employment', 'Contact and job details', 2,
 '📞', true, true, NOW(), NOW()),

(9, 'Compliance', 'compliance', 'Mandatory compliance documents', 3,
 '📋', true, true, NOW(), NOW());

-- Sections for Template 10: Intern Template (Simplified)
INSERT INTO employee_template_section
(template_id, section_name, section_code, section_description, section_order,
 section_icon, is_collapsible, is_expanded_by_default, created_at, updated_at)
VALUES
(10, 'Personal & Contact', 'personal_contact', 'Basic personal and contact information', 1,
 '👤', true, true, NOW(), NOW()),

(10, 'Internship Details', 'internship_details', 'Internship program information', 2,
 '🎓', true, true, NOW(), NOW());

-- Verify sections created
SELECT 'Sections created:' AS status, COUNT(*) as count FROM employee_template_section;

-- ==============================================================================
-- STEP 2: Get Section IDs and Map Fields
-- ==============================================================================

-- Note: We need to get actual section IDs. Let's query them first.
SELECT id, template_id, section_name, section_code FROM employee_template_section ORDER BY template_id, section_order;
