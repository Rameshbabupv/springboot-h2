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