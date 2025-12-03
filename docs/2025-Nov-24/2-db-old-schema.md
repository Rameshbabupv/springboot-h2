# HRMS Database Specification for Spring Boot + H2

## Database: hrms_db

---

## 1. USER MANAGEMENT

### users
- id: BIGINT (PK, Auto)
- username: VARCHAR(100) UNIQUE NOT NULL
- email: VARCHAR(255) UNIQUE NOT NULL
- password_hash: VARCHAR(255) NOT NULL
- first_name: VARCHAR(100) NOT NULL
- last_name: VARCHAR(100) NOT NULL
- mobile_number: VARCHAR(20)
- employee_id: BIGINT (FK → employees.id)
- default_company_id: BIGINT (FK → companies.id)
- user_type: VARCHAR(50) NOT NULL
- is_super_admin: BOOLEAN DEFAULT false
- is_active: BOOLEAN DEFAULT true
- is_locked: BOOLEAN DEFAULT false
- last_login_at: TIMESTAMP
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- updated_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### user_roles
- id: BIGINT (PK, Auto)
- company_id: BIGINT (FK → companies.id, nullable)
- role_name: VARCHAR(100) NOT NULL
- role_code: VARCHAR(50) NOT NULL
- description: TEXT
- is_system_role: BOOLEAN DEFAULT false
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (company_id, role_code)

### user_permissions
- id: BIGINT (PK, Auto)
- user_id: BIGINT (FK → users.id) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- role_id: BIGINT (FK → user_roles.id) NOT NULL
- can_view: BOOLEAN DEFAULT false
- can_create: BOOLEAN DEFAULT false
- can_edit: BOOLEAN DEFAULT false
- can_delete: BOOLEAN DEFAULT false
- can_approve: BOOLEAN DEFAULT false
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (user_id, company_id, role_id)

---

## 2. COMPANY/ORGANIZATION

### companies
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- industry: VARCHAR(100)
- company_name: VARCHAR(255) UNIQUE NOT NULL
- short_name: VARCHAR(100)
- logo_url: VARCHAR(500)
- address_line1: VARCHAR(255)
- address_line2: VARCHAR(255)
- country: VARCHAR(100)
- state: VARCHAR(100)
- city: VARCHAR(100)
- pincode: VARCHAR(20)
- primary_phone: VARCHAR(20)
- email: VARCHAR(255)
- website: VARCHAR(255)
- gst_number: VARCHAR(50) UNIQUE
- pan: VARCHAR(20) UNIQUE
- tan: VARCHAR(20) UNIQUE
- cin: VARCHAR(50)
- incorporation_date: DATE
- company_type: VARCHAR(50)
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- updated_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### company_settings
- id: BIGINT (PK, Auto)
- company_id: BIGINT (FK → companies.id) UNIQUE NOT NULL
- enable_divisions: BOOLEAN DEFAULT false
- enable_department: BOOLEAN DEFAULT true
- enable_section: BOOLEAN DEFAULT false
- enable_grade: BOOLEAN DEFAULT false
- enable_job_function: BOOLEAN DEFAULT false
- enable_employment_type: BOOLEAN DEFAULT true
- currency: VARCHAR(10) DEFAULT 'INR'
- date_format: VARCHAR(20) DEFAULT 'DD/MM/YYYY'
- working_days_per_week: INT DEFAULT 5
- pf_enabled: BOOLEAN DEFAULT false
- pf_employee_rate: DECIMAL(5,2) DEFAULT 12.00
- pf_employer_rate: DECIMAL(5,2) DEFAULT 12.00
- esi_enabled: BOOLEAN DEFAULT false
- esi_employee_rate: DECIMAL(5,2) DEFAULT 0.75
- esi_employer_rate: DECIMAL(5,2) DEFAULT 3.25
- pt_enabled: BOOLEAN DEFAULT false
- enable_ot: BOOLEAN DEFAULT false
- enable_permissions: BOOLEAN DEFAULT true
- payroll_frequency: VARCHAR(20) DEFAULT 'monthly'
- payment_day: VARCHAR(10) DEFAULT '1st'
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- updated_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### company_locations
- id: BIGINT (PK, Auto)
- company_id: BIGINT (FK → companies.id) NOT NULL
- type: VARCHAR(50) NOT NULL
- name: VARCHAR(255) NOT NULL
- code: VARCHAR(50) NOT NULL
- address1: VARCHAR(255)
- state: VARCHAR(100)
- city: VARCHAR(100)
- pincode: VARCHAR(20)
- esi_number: VARCHAR(50)
- pf_number: VARCHAR(50)
- gstin: VARCHAR(50)
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (company_id, code)

### company_bank_accounts
- id: BIGINT (PK, Auto)
- company_id: BIGINT (FK → companies.id) NOT NULL
- beneficiary_name: VARCHAR(255) NOT NULL
- account_name: VARCHAR(255) NOT NULL
- bank_name: VARCHAR(255) NOT NULL
- branch_name: VARCHAR(255)
- account_number: VARCHAR(50) UNIQUE NOT NULL
- ifsc_code: VARCHAR(20) NOT NULL
- account_type: VARCHAR(50) DEFAULT 'Current Account'
- is_primary: BOOLEAN DEFAULT false
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

---

## 3. MASTER DATA (Tenant Level - No company_id)

### divisions
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- name: VARCHAR(100) NOT NULL
- code: VARCHAR(50) NOT NULL
- description: TEXT
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (tenant_id, code)

### departments
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- name: VARCHAR(100) NOT NULL
- code: VARCHAR(50) NOT NULL
- description: TEXT
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (tenant_id, code)

### sections
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- department_id: BIGINT (FK → departments.id) NOT NULL
- name: VARCHAR(100) NOT NULL
- code: VARCHAR(50) NOT NULL
- description: TEXT
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (tenant_id, code)

### designations
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- name: VARCHAR(100) NOT NULL
- code: VARCHAR(50) NOT NULL
- description: TEXT
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (tenant_id, code)

### job_functions
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- name: VARCHAR(100) NOT NULL
- code: VARCHAR(50) NOT NULL
- description: TEXT
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (tenant_id, code)

### employment_types
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- name: VARCHAR(100) NOT NULL
- code: VARCHAR(50) NOT NULL
- description: TEXT
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (tenant_id, code)

### grades
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- name: VARCHAR(100) NOT NULL
- code: VARCHAR(50) NOT NULL
- description: TEXT
- level: INT
- min_salary: DECIMAL(10,2)
- max_salary: DECIMAL(10,2)
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (tenant_id, code)

### cities
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- city_name: VARCHAR(100) NOT NULL
- state: VARCHAR(100) NOT NULL
- country: VARCHAR(100) DEFAULT 'India'
- pincode: VARCHAR(10)
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (tenant_id, city_name, state)

### states
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- name: VARCHAR(100) NOT NULL
- code: VARCHAR(50) NOT NULL
- description: TEXT
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (tenant_id, code)

---

## 4. EMPLOYEE MANAGEMENT

### employees
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- emp_id: VARCHAR(50) UNIQUE NOT NULL
- employee_name: VARCHAR(255) NOT NULL
- gender: VARCHAR(20)
- date_of_birth: DATE
- date_of_join: DATE NOT NULL
- mobile_no: VARCHAR(20)
- email_id: VARCHAR(255)
- blood_group: VARCHAR(10)
- marital_status: VARCHAR(20)
- division_id: BIGINT (FK → divisions.id)
- department_id: BIGINT (FK → departments.id)
- section_id: BIGINT (FK → sections.id)
- designation_id: BIGINT (FK → designations.id)
- job_function_id: BIGINT (FK → job_functions.id)
- employment_type_id: BIGINT (FK → employment_types.id)
- grade_id: BIGINT (FK → grades.id)
- location_id: BIGINT (FK → company_locations.id)
- reporting_manager_id: BIGINT (FK → employees.id)
- basic_salary: DECIMAL(10,2)
- gross_salary: DECIMAL(10,2)
- ctc: DECIMAL(10,2)
- aadhar_no: VARCHAR(12)
- pan_no: VARCHAR(10)
- uan: VARCHAR(50)
- cover_pf: BOOLEAN DEFAULT false
- pf_number: VARCHAR(50)
- cover_esi: BOOLEAN DEFAULT false
- esi_number: VARCHAR(50)
- employee_status: VARCHAR(20) DEFAULT 'Active'
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- updated_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- INDEX: (tenant_id, company_id)

### employee_documents
- id: BIGINT (PK, Auto)
- employee_id: BIGINT (FK → employees.id) NOT NULL
- document_type: VARCHAR(100) NOT NULL
- document_name: VARCHAR(255) NOT NULL
- file_path: VARCHAR(500) NOT NULL
- file_size: BIGINT
- uploaded_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### employee_history
- id: BIGINT (PK, Auto)
- employee_id: BIGINT (FK → employees.id) NOT NULL
- change_type: VARCHAR(50) NOT NULL
- change_date: DATE NOT NULL
- old_value: TEXT
- new_value: TEXT
- remarks: TEXT
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### employee_emergency_contacts
- id: BIGINT (PK, Auto)
- employee_id: BIGINT (FK → employees.id) NOT NULL
- contact_name: VARCHAR(255) NOT NULL
- relationship: VARCHAR(100)
- mobile_number: VARCHAR(20) NOT NULL
- address: TEXT
- is_primary: BOOLEAN DEFAULT false

### employee_education
- id: BIGINT (PK, Auto)
- employee_id: BIGINT (FK → employees.id) NOT NULL
- degree: VARCHAR(255) NOT NULL
- institution: VARCHAR(255)
- year_of_passing: INT
- marks_grade: VARCHAR(50)

---

## 5. ATTENDANCE

### shifts
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- shift_name: VARCHAR(100) NOT NULL
- shift_code: VARCHAR(50) NOT NULL
- start_time: TIME NOT NULL
- end_time: TIME NOT NULL
- working_hours: DECIMAL(4,2) NOT NULL
- break_duration: INT DEFAULT 0
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (company_id, shift_code)

### shift_templates
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- template_name: VARCHAR(100) NOT NULL
- template_code: VARCHAR(50) NOT NULL
- description: TEXT
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### shift_rosters
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- shift_id: BIGINT (FK → shifts.id) NOT NULL
- roster_date: DATE NOT NULL
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (employee_id, roster_date)

### attendance_records
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- attendance_date: DATE NOT NULL
- shift_id: BIGINT (FK → shifts.id)
- in_time: TIME
- out_time: TIME
- work_hours: DECIMAL(4,2)
- status: VARCHAR(50)
- remarks: TEXT
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (employee_id, attendance_date)

### holidays
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- holiday_name: VARCHAR(255) NOT NULL
- holiday_date: DATE NOT NULL
- holiday_type: VARCHAR(50)
- is_mandatory: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### holiday_templates
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- template_name: VARCHAR(100) NOT NULL
- template_code: VARCHAR(50) NOT NULL
- year: INT NOT NULL
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

---

## 6. LEAVE MANAGEMENT

### leave_types
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- leave_name: VARCHAR(100) NOT NULL
- leave_code: VARCHAR(50) NOT NULL
- max_days: INT DEFAULT 0
- is_paid: BOOLEAN DEFAULT true
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (company_id, leave_code)

### leave_templates
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- template_name: VARCHAR(100) NOT NULL
- template_code: VARCHAR(50) NOT NULL
- description: TEXT
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### leave_applications
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- leave_type_id: BIGINT (FK → leave_types.id) NOT NULL
- from_date: DATE NOT NULL
- to_date: DATE NOT NULL
- total_days: DECIMAL(4,1) NOT NULL
- reason: TEXT
- status: VARCHAR(50) DEFAULT 'Pending'
- approved_by: BIGINT (FK → users.id)
- approved_at: TIMESTAMP
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### leave_balances
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- leave_type_id: BIGINT (FK → leave_types.id) NOT NULL
- year: INT NOT NULL
- opening_balance: DECIMAL(4,1) DEFAULT 0
- earned: DECIMAL(4,1) DEFAULT 0
- availed: DECIMAL(4,1) DEFAULT 0
- balance: DECIMAL(4,1) DEFAULT 0
- UNIQUE: (employee_id, leave_type_id, year)

### permissions
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- permission_date: DATE NOT NULL
- from_time: TIME NOT NULL
- to_time: TIME NOT NULL
- duration_hours: DECIMAL(4,2) NOT NULL
- reason: TEXT
- status: VARCHAR(50) DEFAULT 'Pending'
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### compoffs
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- worked_date: DATE NOT NULL
- compoff_date: DATE
- status: VARCHAR(50) DEFAULT 'Earned'
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

---

## 7. PAYROLL

### pay_heads
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- head_name: VARCHAR(100) NOT NULL
- head_code: VARCHAR(50) NOT NULL
- head_type: VARCHAR(50) NOT NULL
- calculation_type: VARCHAR(50)
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (company_id, head_code)

### pay_templates
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- template_name: VARCHAR(100) NOT NULL
- template_code: VARCHAR(50) NOT NULL
- description: TEXT
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### salary_structures
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- pay_template_id: BIGINT (FK → pay_templates.id)
- effective_from: DATE NOT NULL
- basic_salary: DECIMAL(10,2) NOT NULL
- gross_salary: DECIMAL(10,2) NOT NULL
- ctc: DECIMAL(10,2) NOT NULL
- is_active: BOOLEAN DEFAULT true
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### payroll_runs
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- payroll_month: VARCHAR(10) NOT NULL
- payroll_year: INT NOT NULL
- run_date: DATE NOT NULL
- status: VARCHAR(50) DEFAULT 'Draft'
- total_employees: INT DEFAULT 0
- total_gross: DECIMAL(12,2) DEFAULT 0
- total_deductions: DECIMAL(12,2) DEFAULT 0
- total_net: DECIMAL(12,2) DEFAULT 0
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (company_id, payroll_month, payroll_year)

### payroll_transactions
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- payroll_run_id: BIGINT (FK → payroll_runs.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- pay_head_id: BIGINT (FK → pay_heads.id) NOT NULL
- amount: DECIMAL(10,2) NOT NULL
- transaction_type: VARCHAR(50) NOT NULL
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### arrears
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- arrear_type: VARCHAR(100) NOT NULL
- from_month: VARCHAR(10) NOT NULL
- to_month: VARCHAR(10) NOT NULL
- amount: DECIMAL(10,2) NOT NULL
- status: VARCHAR(50) DEFAULT 'Pending'
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### loans
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- loan_type: VARCHAR(100) NOT NULL
- loan_amount: DECIMAL(10,2) NOT NULL
- installment_amount: DECIMAL(10,2) NOT NULL
- installments: INT NOT NULL
- installments_paid: INT DEFAULT 0
- balance_amount: DECIMAL(10,2) NOT NULL
- status: VARCHAR(50) DEFAULT 'Active'
- sanctioned_date: DATE NOT NULL
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### advances
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- advance_type: VARCHAR(100) NOT NULL
- amount: DECIMAL(10,2) NOT NULL
- deduction_amount: DECIMAL(10,2) NOT NULL
- deductions_made: INT DEFAULT 0
- balance_amount: DECIMAL(10,2) NOT NULL
- status: VARCHAR(50) DEFAULT 'Active'
- advance_date: DATE NOT NULL
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

---

## 8. STATUTORY

### pf_records
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- month: VARCHAR(10) NOT NULL
- year: INT NOT NULL
- employee_contribution: DECIMAL(10,2) DEFAULT 0
- employer_contribution: DECIMAL(10,2) DEFAULT 0
- total_contribution: DECIMAL(10,2) DEFAULT 0
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (employee_id, month, year)

### esi_records
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- month: VARCHAR(10) NOT NULL
- year: INT NOT NULL
- employee_contribution: DECIMAL(10,2) DEFAULT 0
- employer_contribution: DECIMAL(10,2) DEFAULT 0
- total_contribution: DECIMAL(10,2) DEFAULT 0
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (employee_id, month, year)

### pt_records
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- month: VARCHAR(10) NOT NULL
- year: INT NOT NULL
- pt_amount: DECIMAL(10,2) DEFAULT 0
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (employee_id, month, year)

### tds_records
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id) NOT NULL
- employee_id: BIGINT (FK → employees.id) NOT NULL
- financial_year: VARCHAR(10) NOT NULL
- month: VARCHAR(10) NOT NULL
- gross_salary: DECIMAL(10,2) DEFAULT 0
- taxable_income: DECIMAL(10,2) DEFAULT 0
- tds_amount: DECIMAL(10,2) DEFAULT 0
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
- UNIQUE: (employee_id, month, financial_year)

---

## 9. INTEGRATIONS & LOGS

### integrations
- id: BIGINT (PK, Auto)
- company_id: BIGINT (FK → companies.id) NOT NULL
- integration_type: VARCHAR(100) NOT NULL
- integration_name: VARCHAR(255) NOT NULL
- api_endpoint: VARCHAR(500)
- api_key: VARCHAR(500)
- is_active: BOOLEAN DEFAULT false
- last_sync_at: TIMESTAMP
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### audit_logs
- id: BIGINT (PK, Auto)
- tenant_id: VARCHAR(100) NOT NULL
- company_id: BIGINT (FK → companies.id)
- user_id: BIGINT (FK → users.id)
- action: VARCHAR(100) NOT NULL
- entity_type: VARCHAR(100) NOT NULL
- entity_id: BIGINT
- old_value: TEXT
- new_value: TEXT
- ip_address: VARCHAR(50)
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

### system_logs
- id: BIGINT (PK, Auto)
- log_level: VARCHAR(20) NOT NULL
- message: TEXT NOT NULL
- stack_trace: TEXT
- created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

---

## IMPORTANT NOTES FOR SPRING BOOT

1. **Multi-Tenancy**: All tables (except user-related) have `tenant_id` column
2. **Company Isolation**: Transactional tables have both `tenant_id` + `company_id`
3. **Master Data**: Only `tenant_id` (shared across companies)
4. **Relationships**: Use JPA `@ManyToOne`, `@OneToMany` annotations
5. **Timestamps**: Use `@CreatedDate`, `@LastModifiedDate` with auditing
6. **Soft Delete**: Use `is_active` flag instead of hard delete
7. **Validation**: Use `@NotNull`, `@Size`, `@Email`, `@Pattern` annotations
8. **Indexes**: Add `@Table(indexes={...})` for performance
9. **H2 Database**: Use `spring.datasource.url=jdbc:h2:mem:hrms_db`
10. **Data Loading**: Use `import.sql` or `@PostConstruct` for seed data

---

## SEED DATA PRIORITY

1. Create super admin user
2. Create default company
3. Create default company_settings
4. Create master data (states, cities, departments, etc.)
5. Create sample employees

---

**Total Tables: 48**
