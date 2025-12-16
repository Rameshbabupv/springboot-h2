-- =============================================================================
-- Leave Type Master - Seed Data
-- =============================================================================
-- This script inserts 12 standard leave types as per HRMS requirements.
-- These are tenant-wide shared leave types (company_id = NULL).
-- Execute this after creating the leave_types table.
-- =============================================================================

-- Note: Replace 'TENANT_ID_HERE' with actual tenant ID when inserting
-- For multi-tenant setup, run this script for each tenant with their tenant_id

-- =============================================================================
-- DEFINED CATEGORY (6 types)
-- Fixed annual quota leave types
-- =============================================================================

INSERT INTO leave_types (tenant_id, company_id, code, name, description, category, color_code, icon, display_order, is_active, created_by, updated_by)
VALUES
('TENANT_ID_HERE', NULL, 'CL', 'Casual Leave',
 'Short-term planned leave for personal needs, rest, or emergencies',
 'DEFINED', '#3B82F6', '🌴', 1, true, 'SYSTEM', 'SYSTEM'),

('TENANT_ID_HERE', NULL, 'SL', 'Sick Leave',
 'Leave for medical illness, injury, or health-related issues',
 'DEFINED', '#EF4444', '🤒', 2, true, 'SYSTEM', 'SYSTEM'),

('TENANT_ID_HERE', NULL, 'ML', 'Maternity Leave',
 'Leave for childbirth and postnatal recovery (as per law)',
 'DEFINED', '#EC4899', '👶', 3, true, 'SYSTEM', 'SYSTEM'),

('TENANT_ID_HERE', NULL, 'PAT', 'Paternity Leave',
 'Leave for fathers during/after childbirth',
 'DEFINED', '#06B6D4', '👶', 4, true, 'SYSTEM', 'SYSTEM'),

('TENANT_ID_HERE', NULL, 'MR', 'Marriage Leave',
 'Leave for employee marriage or immediate family member marriage',
 'DEFINED', '#F59E0B', '💍', 5, true, 'SYSTEM', 'SYSTEM'),

('TENANT_ID_HERE', NULL, 'BL', 'Bereavement Leave',
 'Compassionate leave for death of immediate family member',
 'DEFINED', '#6B7280', '🏳️', 6, true, 'SYSTEM', 'SYSTEM');

-- =============================================================================
-- EARNED CATEGORY (2 types)
-- Leave types accrued based on working days
-- =============================================================================

INSERT INTO leave_types (tenant_id, company_id, code, name, description, category, color_code, icon, display_order, is_active, created_by, updated_by)
VALUES
('TENANT_ID_HERE', NULL, 'EL', 'Earned Leave',
 'Accrued leave based on days worked, can be carried forward or encashed',
 'EARNED', '#10B981', '⭐', 7, true, 'SYSTEM', 'SYSTEM'),

('TENANT_ID_HERE', NULL, 'PL', 'Privilege Leave',
 'Privileged earned leave with encashment and carry-forward benefits',
 'EARNED', '#8B5CF6', '✈️', 8, true, 'SYSTEM', 'SYSTEM');

-- =============================================================================
-- UNLIMITED CATEGORY (1 type)
-- No limit on days but unpaid
-- =============================================================================

INSERT INTO leave_types (tenant_id, company_id, code, name, description, category, color_code, icon, display_order, is_active, created_by, updated_by)
VALUES
('TENANT_ID_HERE', NULL, 'LOP', 'Loss of Pay',
 'Unpaid leave when all other leave balances are exhausted',
 'UNLIMITED', '#DC2626', '➖', 9, true, 'SYSTEM', 'SYSTEM');

-- =============================================================================
-- COMPENSATORY CATEGORY (1 type)
-- Compensatory off for working on holidays/weekends
-- =============================================================================

INSERT INTO leave_types (tenant_id, company_id, code, name, description, category, color_code, icon, display_order, is_active, created_by, updated_by)
VALUES
('TENANT_ID_HERE', NULL, 'CO', 'Comp-Off',
 'Compensatory leave for working on holidays or weekends',
 'COMPENSATORY', '#14B8A6', '🎁', 10, true, 'SYSTEM', 'SYSTEM');

-- =============================================================================
-- PERMISSION CATEGORY (2 types)
-- Short duration permissions (less than a day)
-- =============================================================================

INSERT INTO leave_types (tenant_id, company_id, code, name, description, category, color_code, icon, display_order, is_active, created_by, updated_by)
VALUES
('TENANT_ID_HERE', NULL, 'PRM', 'Permission',
 'Short duration permission for personal work (2-4 hours)',
 'PERMISSION', '#F97316', '⏰', 11, true, 'SYSTEM', 'SYSTEM'),

('TENANT_ID_HERE', NULL, 'OFP', 'Office Permission',
 'Permission for office-related work outside premises',
 'PERMISSION', '#0EA5E9', '💼', 12, true, 'SYSTEM', 'SYSTEM');

-- =============================================================================
-- VERIFICATION QUERY
-- =============================================================================
-- Run this query to verify all 12 leave types were inserted successfully:
--
-- SELECT
--     code,
--     name,
--     category,
--     color_code,
--     icon,
--     display_order,
--     is_active
-- FROM leave_types
-- WHERE tenant_id = 'TENANT_ID_HERE'
--   AND company_id IS NULL
-- ORDER BY display_order;
--
-- Expected result: 12 rows
-- =============================================================================

-- =============================================================================
-- NOTES FOR BACKEND TEAM
-- =============================================================================
-- 1. Before running this script, replace 'TENANT_ID_HERE' with actual tenant ID
-- 2. These are shared (tenant-wide) leave types with company_id = NULL
-- 3. To create company-specific leave types, set company_id to the company's ID
-- 4. Color codes follow Tailwind CSS color palette for consistency
-- 5. Display order determines the sort order in UI dropdowns
-- 6. All leave types are active by default (is_active = true)
-- 7. created_by and updated_by are set to 'SYSTEM' for seed data
-- =============================================================================
