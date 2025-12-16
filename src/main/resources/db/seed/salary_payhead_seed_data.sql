--
-- Payhead Master - Seed Data Script
-- Author: Claude Sonnet 4.5
-- Date: December 16, 2025
-- Purpose: Standard payheads for Indian payroll (30 payheads)
--
-- Usage: Run this script after database schema is created
-- Note: tenant_id = 'SYSTEM' and company_id = NULL means system-wide defaults
--

-- ==========================================
-- EARNING COMPONENTS (15 payheads)
-- ==========================================

-- Fixed Salary Components
INSERT INTO payhead_master (tenant_id, company_id, payhead_name, payhead_code, payhead_type, payhead_category, calculation_type, default_value, is_taxable, affects_pf, affects_esi, affects_gratuity, affects_lwf, display_order, show_in_payslip, is_mandatory, is_active, created_by, created_at, updated_by, updated_at) VALUES
('SYSTEM', NULL, 'Basic Salary', 'BASIC', 'EARNING', 'Fixed Allowances', 'FIXED', 0, true, true, true, true, false, 1, true, true, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Dearness Allowance', 'DA', 'EARNING', 'Fixed Allowances', 'PERCENTAGE', 12, true, true, true, true, false, 2, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'House Rent Allowance', 'HRA', 'EARNING', 'Fixed Allowances', 'PERCENTAGE', 40, true, false, true, false, false, 3, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Conveyance Allowance', 'CONV', 'EARNING', 'Fixed Allowances', 'FIXED', 1600, true, false, true, false, false, 4, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Medical Allowance', 'MED', 'EARNING', 'Fixed Allowances', 'FIXED', 1250, true, false, true, false, false, 5, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Special Allowance', 'SPECIAL', 'EARNING', 'Fixed Allowances', 'FIXED', 0, true, false, true, false, false, 6, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Education Allowance', 'EDU', 'EARNING', 'Fixed Allowances', 'FIXED', 1000, true, false, true, false, false, 7, true, false, true, 1, NOW(), 1, NOW());

-- Variable Pay Components
INSERT INTO payhead_master (tenant_id, company_id, payhead_name, payhead_code, payhead_type, payhead_category, calculation_type, default_value, is_taxable, affects_pf, affects_esi, affects_gratuity, affects_lwf, display_order, show_in_payslip, is_mandatory, is_active, created_by, created_at, updated_by, updated_at) VALUES
('SYSTEM', NULL, 'Performance Bonus', 'BONUS', 'EARNING', 'Variable Pay', 'FIXED', 0, true, false, false, false, false, 10, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Incentive', 'INC', 'EARNING', 'Variable Pay', 'FIXED', 0, true, false, false, false, false, 11, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Overtime Pay', 'OT', 'EARNING', 'Variable Pay', 'FIXED', 0, true, false, true, false, false, 12, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Shift Allowance', 'SHIFT', 'EARNING', 'Variable Pay', 'FIXED', 0, true, false, true, false, false, 13, true, false, true, 1, NOW(), 1, NOW());

-- Reimbursements (Non-Taxable)
INSERT INTO payhead_master (tenant_id, company_id, payhead_name, payhead_code, payhead_type, payhead_category, calculation_type, default_value, is_taxable, affects_pf, affects_esi, affects_gratuity, affects_lwf, display_order, show_in_payslip, is_mandatory, is_active, created_by, created_at, updated_by, updated_at) VALUES
('SYSTEM', NULL, 'Telephone Reimbursement', 'TEL_REIMB', 'EARNING', 'Reimbursements', 'FIXED', 0, false, false, false, false, false, 20, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Fuel Reimbursement', 'FUEL_REIMB', 'EARNING', 'Reimbursements', 'FIXED', 0, false, false, false, false, false, 21, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Leave Encashment', 'LEAVE_ENC', 'EARNING', 'Other Earnings', 'FIXED', 0, true, false, false, false, false, 22, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Arrears', 'ARREARS', 'EARNING', 'Other Earnings', 'FIXED', 0, true, false, false, false, false, 23, true, false, true, 1, NOW(), 1, NOW());

-- ==========================================
-- DEDUCTION COMPONENTS (10 payheads)
-- ==========================================

-- Statutory Deductions
INSERT INTO payhead_master (tenant_id, company_id, payhead_name, payhead_code, payhead_type, payhead_category, calculation_type, calculation_base, default_value, is_taxable, affects_pf, affects_esi, affects_gratuity, affects_lwf, display_order, show_in_payslip, is_mandatory, is_active, created_by, created_at, updated_by, updated_at) VALUES
('SYSTEM', NULL, 'Employee Provident Fund', 'EPF', 'DEDUCTION', 'Statutory', 'PERCENTAGE', 'BASIC', 12, false, false, false, false, false, 1, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Employee State Insurance', 'ESIC', 'DEDUCTION', 'Statutory', 'PERCENTAGE', 'GROSS', 0.75, false, false, false, false, false, 2, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Professional Tax', 'PT', 'DEDUCTION', 'Statutory', 'FIXED', NULL, 200, false, false, false, false, false, 3, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Tax Deducted at Source', 'TDS', 'DEDUCTION', 'Statutory', 'FIXED', NULL, 0, false, false, false, false, false, 4, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Labour Welfare Fund', 'LWF', 'DEDUCTION', 'Statutory', 'FIXED', NULL, 20, false, false, false, false, false, 5, true, false, true, 1, NOW(), 1, NOW());

-- Other Deductions
INSERT INTO payhead_master (tenant_id, company_id, payhead_name, payhead_code, payhead_type, payhead_category, calculation_type, default_value, is_taxable, affects_pf, affects_esi, affects_gratuity, affects_lwf, display_order, show_in_payslip, is_mandatory, is_active, created_by, created_at, updated_by, updated_at) VALUES
('SYSTEM', NULL, 'Loan Deduction', 'LOAN', 'DEDUCTION', 'Other', 'FIXED', 0, false, false, false, false, false, 10, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Advance Deduction', 'ADVANCE', 'DEDUCTION', 'Other', 'FIXED', 0, false, false, false, false, false, 11, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Fine/Penalty', 'FINE', 'DEDUCTION', 'Other', 'FIXED', 0, false, false, false, false, false, 12, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Loss of Pay', 'LOP', 'DEDUCTION', 'Other', 'FIXED', 0, false, false, false, false, false, 13, true, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Other Deductions', 'OTHER_DED', 'DEDUCTION', 'Other', 'FIXED', 0, false, false, false, false, false, 14, true, false, true, 1, NOW(), 1, NOW());

-- ==========================================
-- STATUTORY COMPONENTS (5 payheads)
-- Employer Contributions (Add to CTC, not paid to employee)
-- ==========================================

INSERT INTO payhead_master (tenant_id, company_id, payhead_name, payhead_code, payhead_type, payhead_category, calculation_type, calculation_base, default_value, is_taxable, affects_pf, affects_esi, affects_gratuity, affects_lwf, display_order, show_in_payslip, is_mandatory, is_active, created_by, created_at, updated_by, updated_at) VALUES
('SYSTEM', NULL, 'Employer PF Contribution', 'ER_PF', 'STATUTORY', 'Employer Contribution', 'PERCENTAGE', 'BASIC', 12, false, false, false, false, false, 1, false, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Employer ESI Contribution', 'ER_ESI', 'STATUTORY', 'Employer Contribution', 'PERCENTAGE', 'GROSS', 3.25, false, false, false, false, false, 2, false, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Gratuity', 'GRATUITY', 'STATUTORY', 'Employer Contribution', 'PERCENTAGE', 'BASIC', 4.81, false, false, false, false, false, 3, false, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Labour Welfare Fund (Employer)', 'ER_LWF', 'STATUTORY', 'Employer Contribution', 'FIXED', NULL, 40, false, false, false, false, false, 4, false, false, true, 1, NOW(), 1, NOW()),
('SYSTEM', NULL, 'Other Statutory Contribution', 'OTHER_STAT', 'STATUTORY', 'Employer Contribution', 'FIXED', NULL, 0, false, false, false, false, false, 5, false, false, true, 1, NOW(), 1, NOW());

-- ==========================================
-- Verification Query
-- ==========================================

-- Run this to verify seed data was inserted correctly:
-- SELECT payhead_type, COUNT(*) as count
-- FROM payhead_master
-- WHERE tenant_id = 'SYSTEM'
-- GROUP BY payhead_type;

-- Expected results:
-- EARNING: 15
-- DEDUCTION: 10
-- STATUTORY: 5
-- Total: 30
