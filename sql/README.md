# Company Setup Module - Database Schema

## Overview
Complete database schema for the Company Setup module with 5 tables covering all aspects of company management.

## Execution Status: ✅ ALL TABLES CREATED

### Tables Created

| # | Table Name | Columns | Description | Foreign Keys |
|---|------------|---------|-------------|--------------|
| 1 | `company` | 28 | Main company master table | None |
| 2 | `company_statutory` | 30 | Tax IDs and statutory compliance | company.id |
| 3 | `company_general_settings` | 13 | Org structure and system config | company.id |
| 4 | `company_location` | 21 | Multiple company locations | company.id |
| 5 | `company_bank_account` | 13 | Bank accounts for payroll | company.id |

**Total Columns:** 105 columns across 5 tables

## Table Relationships

```
company (1)
    ├── company_statutory (1:1)
    ├── company_general_settings (1:1)
    ├── company_location (1:N)
    └── company_bank_account (1:N)
```

## Table Details

### 1. company
**Purpose:** Main company/organization master table

**Key Fields:**
- `id` (PK) - Auto-generated company ID
- `tenant_id` - Multi-tenant identifier
- `code` - Unique company code
- `name` - Official company name
- `short_name` - Abbreviation
- `company_type` - Private Limited, Public Limited, LLP, etc.
- `logo` - Company logo URL
- Address fields (line1, line2, city, state, country, pincode)
- Contact information (phone, email, website)
- Point of contact details
- `is_active` - Status flag

**Constraints:**
- UNIQUE (tenant_id, code)
- UNIQUE (tenant_id, name)

**Indexes:**
- tenant_id
- code
- name
- is_active

---

### 2. company_statutory
**Purpose:** Tax IDs and compliance configuration

**Key Fields:**

*Tax IDs:*
- `pan` - Permanent Account Number (10 chars, validated)
- `tan` - Tax Deduction Account Number (10 chars, validated)
- `cin` - Corporate Identity Number
- `lin` - Labour Identification Number
- `gstin` - GST Identification Number

*PF (Provident Fund) Configuration:*
- `pf_enabled` - Enable/disable PF
- `pf_account_number` - PF account
- `pf_ceiling` - Ceiling amount (default: 15000)
- `pf_employee_rate` - Employee contribution % (default: 12.00)
- `pf_employer_rate` - Employer contribution % (default: 12.00)
- `pf_employer_epf_rate` - EPF rate (default: 3.67)
- `pf_employer_eps_rate` - EPS rate (default: 8.33)

*ESI (Employee State Insurance):*
- `esi_enabled` - Enable/disable ESI
- `esi_number` - ESI number
- `esi_ceiling` - Ceiling amount (default: 21000)
- `esi_employee_rate` - Employee rate (default: 0.75)
- `esi_employer_rate` - Employer rate (default: 3.25)

*PT (Professional Tax):*
- `pt_enabled` - Enable/disable PT
- `pt_state` - State for PT
- `pt_registration_number` - PT registration
- `pt_registration_date` - Registration date
- `pt_valid_upto` - Validity date

*Other:*
- `retirement_age` - Default: 60
- `tds_type` - TDS type
- `allow_tds_override` - Allow override flag
- `applicable_acts` - Comma-separated acts

**Constraints:**
- UNIQUE (company_id) - One statutory record per company
- Foreign Key to company(id) with CASCADE DELETE

**Validations:**
- PAN format: ^[A-Z]{5}[0-9]{4}[A-Z]$
- TAN format: ^[A-Z]{4}[0-9]{5}[A-Z]$

---

### 3. company_general_settings
**Purpose:** Organizational structure and system preferences

**Key Fields:**

*Org Structure Flags:*
- `enable_divisions` - Enable divisions (default: false)
- `enable_department` - Enable departments (default: true)
- `enable_section` - Enable sections (default: false)
- `enable_grade` - Enable grades (default: false)

*System Configuration:*
- `currency` - Currency code (default: INR)
- `date_format` - Date format (default: DD/MM/YYYY)
- `time_zone` - Timezone (default: Asia/Kolkata)
- `financial_year_start` - FY start month (default: April)
- `language` - Language code (default: en)

**Constraints:**
- UNIQUE (company_id) - One settings record per company
- Foreign Key to company(id) with CASCADE DELETE

---

### 4. company_location
**Purpose:** Multiple company locations (branches, factories, etc.)

**Key Fields:**

*Basic Info:*
- `type` - Head Office, Branch, Factory, Warehouse, Store
- `name` - Location name
- `code` - Location code

*Address:*
- `address_line1`, `address_line2`
- `state`, `city`, `pincode`

*Location-Specific Statutory:*
- `esi_number` - Location-specific ESI
- `pf_number` - Location-specific PF
- `pt_number` - Location-specific PT
- `gstin` - Location-specific GSTIN
- `license_number` - License number

*Contact Person:*
- `contact_name` (required)
- `contact_phone` (required)
- `contact_email`

*Status:*
- `is_active` - Active flag

**Constraints:**
- UNIQUE (company_id, code)
- Foreign Key to company(id) with CASCADE DELETE
- CHECK type IN ('Head Office', 'Branch', 'Factory', 'Warehouse', 'Store')

**Indexes:**
- company_id
- type
- is_active
- city

---

### 5. company_bank_account
**Purpose:** Bank accounts for payroll and transactions

**Key Fields:**

*Account Details:*
- `beneficiary_name` (required)
- `account_name` (required)
- `bank_name` (required)
- `branch_name`
- `account_number` (required, max 18 chars)
- `ifsc_code` (required, validated)
- `account_type` - Current Account, Cash Credit, Overdraft

*Flags:*
- `is_primary` - Primary account flag (default: false)
- `is_active` - Active flag (default: true)

**Constraints:**
- UNIQUE (company_id, account_number)
- Foreign Key to company(id) with CASCADE DELETE
- CHECK ifsc_code format: ^[A-Z]{4}0[A-Z0-9]{6}$
- CHECK account_type IN ('Current Account', 'Cash Credit', 'Overdraft')

**Indexes:**
- company_id
- is_primary
- is_active

---

## How to Execute

### Option 1: Execute All Tables at Once
```bash
cd /home/sysadmin/data/projects/HRMS_New_Api/sql
PGPASSWORD=Admin@123 psql -h localhost -p 5432 -U postgres -d hrmsdb -f 00_run_all.sql
```

### Option 2: Execute One by One
```bash
cd /home/sysadmin/data/projects/HRMS_New_Api/sql

# Table 1: Company
PGPASSWORD=Admin@123 psql -h localhost -p 5432 -U postgres -d hrmsdb -f 01_company_table.sql

# Table 2: Statutory
PGPASSWORD=Admin@123 psql -h localhost -p 5432 -U postgres -d hrmsdb -f 02_company_statutory_table.sql

# Table 3: General Settings
PGPASSWORD=Admin@123 psql -h localhost -p 5432 -U postgres -d hrmsdb -f 03_company_general_settings_table.sql

# Table 4: Locations
PGPASSWORD=Admin@123 psql -h localhost -p 5432 -U postgres -d hrmsdb -f 04_company_location_table.sql

# Table 5: Bank Accounts
PGPASSWORD=Admin@123 psql -h localhost -p 5432 -U postgres -d hrmsdb -f 05_company_bank_account_table.sql
```

## Verification Commands

### List all company tables
```bash
PGPASSWORD=Admin@123 psql -h localhost -p 5432 -U postgres -d hrmsdb -c "\dt company*"
```

### View table structure
```bash
PGPASSWORD=Admin@123 psql -h localhost -p 5432 -U postgres -d hrmsdb -c "\d company"
PGPASSWORD=Admin@123 psql -h localhost -p 5432 -U postgres -d hrmsdb -c "\d company_statutory"
PGPASSWORD=Admin@123 psql -h localhost -p 5432 -U postgres -d hrmsdb -c "\d company_general_settings"
PGPASSWORD=Admin@123 psql -h localhost -p 5432 -U postgres -d hrmsdb -c "\d company_location"
PGPASSWORD=Admin@123 psql -h localhost -p 5432 -U postgres -d hrmsdb -c "\d company_bank_account"
```

### Check foreign key relationships
```sql
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_name LIKE 'company%';
```

## API Endpoints

The Spring Boot application provides both REST and GraphQL endpoints:

### REST API
- `GET /api/companies` - List all companies
- `GET /api/companies/{id}` - Get company by ID
- `POST /api/companies` - Create company
- `PUT /api/companies/{id}` - Update company
- `DELETE /api/companies/{id}` - Delete company
- `GET/PUT /api/companies/{companyId}/statutory` - Statutory details
- `GET/PUT /api/companies/{companyId}/settings` - General settings
- `GET/POST/PUT/DELETE /api/companies/{companyId}/locations` - Locations
- `GET/POST/PUT/DELETE /api/companies/{companyId}/bank-accounts` - Bank accounts

### GraphQL
- Endpoint: `POST /graphql`
- Queries: companies, company, companyByCode, etc.
- Mutations: createCompany, updateCompany, deleteCompany, etc.

## Notes

1. **Cascade Delete:** All child tables have CASCADE DELETE - when a company is deleted, all related records are automatically deleted
2. **Validation:** PAN, TAN, and IFSC codes have regex validation at database level
3. **Defaults:** Many fields have sensible defaults (PF ceiling: 15000, ESI ceiling: 21000, etc.)
4. **Audit Fields:** All tables have created_at and updated_at timestamps
5. **Multi-tenant:** Main company table supports multi-tenancy via tenant_id

## Created By
Generated on: 2025-11-25
Location: /home/sysadmin/data/projects/HRMS_New_Api/sql/
