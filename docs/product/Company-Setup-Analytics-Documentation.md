# Company Setup Module - Complete Analytics & Database Documentation

**Document Version:** 1.0
**Last Updated:** November 26, 2025
**Module:** Company Setup
**Database:** PostgreSQL

---

## Table of Contents

1. [Module Overview](#module-overview)
2. [Database Schema](#database-schema)
3. [Entity Relationships](#entity-relationships)
4. [Table Details](#table-details)
5. [Business Logic & Rules](#business-logic--rules)
6. [API Endpoints](#api-endpoints)
7. [Analytics & Reporting](#analytics--reporting)
8. [Data Flow Diagrams](#data-flow-diagrams)

---

## Module Overview

### Purpose
The Company Setup module manages comprehensive company information in a multi-tenant HRMS SaaS application. It handles company master data, statutory compliance details, organizational settings, multiple locations, and banking information.

### Key Features
- Multi-tenant company management
- Statutory compliance tracking (PF, ESI, PT, TDS)
- Multiple location support
- Multiple bank account management
- Organizational structure configuration
- Tax identifier management

### Module Components
1. **Company Master** - Core company information
2. **Statutory Details** - Tax and compliance information
3. **General Settings** - Organizational preferences
4. **Locations** - Multi-location support
5. **Bank Accounts** - Financial account management

---

## Database Schema

### Tables Overview

| Table Name | Purpose | Relationship Type | Record Type |
|------------|---------|-------------------|-------------|
| `company` | Core company data | Parent | One per company |
| `company_statutory` | Tax & compliance details | One-to-One | One per company |
| `company_general_settings` | Organization preferences | One-to-One | One per company |
| `company_location` | Physical locations | One-to-Many | Multiple per company |
| `company_bank_account` | Bank account details | One-to-Many | Multiple per company |

---

## Entity Relationships

```
┌─────────────────────┐
│      COMPANY        │ (Parent Entity)
│  - id (PK)          │
│  - tenant_id        │
│  - code (UK)        │
│  - name             │
└──────────┬──────────┘
           │
           ├──────── One-to-One ────────┐
           │                             │
           │                             ▼
           │                   ┌──────────────────────┐
           │                   │ COMPANY_STATUTORY    │
           │                   │ - id (PK)            │
           │                   │ - company_id (FK,UK) │
           │                   └──────────────────────┘
           │
           ├──────── One-to-One ────────┐
           │                             │
           │                             ▼
           │                   ┌─────────────────────────────┐
           │                   │ COMPANY_GENERAL_SETTINGS    │
           │                   │ - id (PK)                   │
           │                   │ - company_id (FK,UK)        │
           │                   └─────────────────────────────┘
           │
           ├──────── One-to-Many ───────┐
           │                             │
           │                             ▼
           │                   ┌──────────────────────┐
           │                   │ COMPANY_LOCATION     │
           │                   │ - id (PK)            │
           │                   │ - company_id (FK)    │
           │                   │ [Multiple Records]   │
           │                   └──────────────────────┘
           │
           └──────── One-to-Many ───────┐
                                        │
                                        ▼
                               ┌──────────────────────┐
                               │ COMPANY_BANK_ACCOUNT │
                               │ - id (PK)            │
                               │ - company_id (FK)    │
                               │ [Multiple Records]   │
                               └──────────────────────┘
```

---

## Table Details

### 1. COMPANY (Parent Table)

**Purpose:** Stores core company information including identity, contact, and address details.

#### Schema Definition

| Column | Data Type | Length | Nullable | Default | Description |
|--------|-----------|--------|----------|---------|-------------|
| `id` | BIGINT | - | NOT NULL | AUTO | Primary Key |
| `tenant_id` | VARCHAR | 50 | NOT NULL | - | Multi-tenancy identifier |
| `code` | VARCHAR | 20 | NOT NULL | - | Unique company code |
| `name` | VARCHAR | 200 | NOT NULL | - | Company legal name |
| `short_name` | VARCHAR | 10 | NULL | - | Company abbreviation |
| `industry` | VARCHAR | 50 | NULL | - | Industry category (dropdown) |
| `industry_description` | VARCHAR | 255 | NULL | - | Detailed industry description |
| `company_type` | VARCHAR | 50 | NULL | - | Legal entity type |
| `logo` | TEXT | - | NULL | - | Company logo (Base64/URL) |
| `address_line1` | VARCHAR | 255 | NULL | - | Registered address line 1 |
| `address_line2` | VARCHAR | 255 | NULL | - | Registered address line 2 |
| `country` | VARCHAR | 100 | NULL | 'India' | Country |
| `state` | VARCHAR | 100 | NULL | - | State/Province |
| `city` | VARCHAR | 100 | NULL | - | City |
| `pincode` | VARCHAR | 10 | NULL | - | Postal code |
| `primary_phone` | VARCHAR | 15 | NULL | - | Primary contact number |
| `alternate_phone` | VARCHAR | 15 | NULL | - | Alternate contact number |
| `email` | VARCHAR | 100 | NULL | - | Official email |
| `website` | VARCHAR | 200 | NULL | - | Company website |
| `contact_name` | VARCHAR | 100 | NULL | - | Primary contact person |
| `contact_designation` | VARCHAR | 100 | NULL | - | Contact person designation |
| `contact_email` | VARCHAR | 100 | NULL | - | Contact person email |
| `contact_phone` | VARCHAR | 15 | NULL | - | Contact person phone |
| `admin_notes` | VARCHAR | 500 | NULL | - | Internal administrative notes |
| `is_active` | BOOLEAN | - | NULL | TRUE | Active status flag |
| `created_by` | VARCHAR | 100 | NULL | - | Created by user |
| `created_at` | TIMESTAMP | - | NULL | CURRENT | Creation timestamp |
| `updated_by` | VARCHAR | 100 | NULL | - | Last updated by user |
| `updated_at` | TIMESTAMP | - | NULL | CURRENT | Last update timestamp |

#### Constraints
- **Primary Key:** `id`
- **Unique Constraints:**
  - `code` (Unique globally)
  - `(tenant_id, code)` (Unique per tenant)

#### Indexes
- PRIMARY KEY on `id`
- UNIQUE INDEX on `code`
- UNIQUE INDEX on `(tenant_id, code)`

#### Business Rules
1. Company `code` must be unique across all tenants
2. `tenant_id` and `name` are mandatory
3. Default `is_active` is TRUE
4. `country` defaults to 'India'
5. Industry can be both category (dropdown) and free text description

---

### 2. COMPANY_STATUTORY (One-to-One)

**Purpose:** Manages tax identifiers and statutory compliance settings including PF, ESI, PT, and TDS configurations.

#### Schema Definition

| Column | Data Type | Length | Nullable | Default | Description |
|--------|-----------|--------|----------|---------|-------------|
| `id` | BIGINT | - | NOT NULL | AUTO | Primary Key |
| `company_id` | BIGINT | - | NOT NULL | - | Foreign Key to company.id |
| **Tax Identifiers** |
| `pan` | VARCHAR | 10 | NULL | - | Permanent Account Number |
| `tan` | VARCHAR | 10 | NULL | - | Tax Deduction Account Number |
| `cin` | VARCHAR | 21 | NULL | - | Corporate Identification Number |
| `lin` | VARCHAR | 21 | NULL | - | Labour Identification Number |
| `gstin` | VARCHAR | 15 | NULL | - | GST Identification Number |
| **PF (Provident Fund) Configuration** |
| `pf_enabled` | BOOLEAN | - | NULL | FALSE | PF applicability flag |
| `pf_account_number` | VARCHAR | 25 | NULL | - | PF account number |
| `pf_ceiling` | DECIMAL | 10,2 | NULL | - | PF wage ceiling amount |
| `pf_employee_rate` | DECIMAL | 5,2 | NULL | - | Employee contribution % |
| `pf_employer_rate` | DECIMAL | 5,2 | NULL | - | Total employer contribution % |
| `pf_employer_epf_rate` | DECIMAL | 5,2 | NULL | - | EPF portion % |
| `pf_employer_eps_rate` | DECIMAL | 5,2 | NULL | - | EPS portion % |
| **ESI (Employee State Insurance) Configuration** |
| `esi_enabled` | BOOLEAN | - | NULL | FALSE | ESI applicability flag |
| `esi_number` | VARCHAR | 17 | NULL | - | ESI registration number |
| `esi_ceiling` | DECIMAL | 10,2 | NULL | - | ESI wage ceiling amount |
| `esi_employee_rate` | DECIMAL | 5,2 | NULL | - | Employee contribution % |
| `esi_employer_rate` | DECIMAL | 5,2 | NULL | - | Employer contribution % |
| **PT (Professional Tax) Configuration** |
| `pt_enabled` | BOOLEAN | - | NULL | FALSE | PT applicability flag |
| `pt_state` | VARCHAR | 50 | NULL | - | PT applicable state |
| `pt_registration_number` | VARCHAR | 30 | NULL | - | PT registration number |
| `pt_registration_date` | DATE | - | NULL | - | PT registration date |
| `pt_valid_upto` | DATE | - | NULL | - | PT validity date |
| **HR & TDS Configuration** |
| `retirement_age` | INTEGER | - | NULL | 58 | Default retirement age |
| `tds_type` | VARCHAR | 10 | NULL | - | TDS calculation type |
| `allow_tds_override` | BOOLEAN | - | NULL | FALSE | Allow TDS override flag |
| `applicable_acts` | TEXT | - | NULL | - | Applicable labour acts (JSON) |
| **Audit Fields** |
| `created_at` | TIMESTAMP | - | NULL | CURRENT | Creation timestamp |
| `updated_at` | TIMESTAMP | - | NULL | CURRENT | Last update timestamp |

#### Constraints
- **Primary Key:** `id`
- **Foreign Key:** `company_id` → `company(id)`
- **Unique Constraint:** `company_id` (One-to-One relationship)

#### Business Rules
1. One statutory record per company (strictly enforced)
2. PAN format: 10 characters (AAAAA9999A)
3. TAN format: 10 characters (AAAA99999A)
4. CIN format: 21 characters
5. GSTIN format: 15 characters
6. PF rates typically: Employee 12%, Employer 12% (EPF 8.33%, EPS 3.67%)
7. ESI rates typically: Employee 0.75%, Employer 3.25%

---

### 3. COMPANY_GENERAL_SETTINGS (One-to-One)

**Purpose:** Stores organizational preferences and system configuration settings.

#### Schema Definition

| Column | Data Type | Length | Nullable | Default | Description |
|--------|-----------|--------|----------|---------|-------------|
| `id` | BIGINT | - | NOT NULL | AUTO | Primary Key |
| `company_id` | BIGINT | - | NOT NULL | - | Foreign Key to company.id |
| **Organizational Structure Toggles** |
| `enable_divisions` | BOOLEAN | - | NULL | TRUE | Enable divisions module |
| `enable_department` | BOOLEAN | - | NULL | TRUE | Enable departments module |
| `enable_section` | BOOLEAN | - | NULL | TRUE | Enable sections module |
| `enable_grade` | BOOLEAN | - | NULL | TRUE | Enable grades module |
| **System Configuration** |
| `currency` | VARCHAR | 3 | NULL | 'INR' | Default currency code (ISO 4217) |
| `date_format` | VARCHAR | 15 | NULL | 'DD-MM-YYYY' | Date display format |
| `time_zone` | VARCHAR | 50 | NULL | 'Asia/Kolkata' | System timezone |
| `financial_year_start` | VARCHAR | 15 | NULL | 'April' | Financial year start month |
| `language` | VARCHAR | 5 | NULL | 'en' | Default language code (ISO 639-1) |
| **Audit Fields** |
| `created_at` | TIMESTAMP | - | NULL | CURRENT | Creation timestamp |
| `updated_at` | TIMESTAMP | - | NULL | CURRENT | Last update timestamp |

#### Constraints
- **Primary Key:** `id`
- **Foreign Key:** `company_id` → `company(id)`
- **Unique Constraint:** `company_id` (One-to-One relationship)

#### Business Rules
1. One settings record per company
2. All organizational modules enabled by default
3. Currency code must be valid ISO 4217
4. Language code must be valid ISO 639-1
5. Timezone must be valid IANA timezone identifier

#### Common Values
- **Currency:** INR, USD, EUR, GBP
- **Date Formats:** DD-MM-YYYY, MM-DD-YYYY, YYYY-MM-DD
- **Timezones:** Asia/Kolkata, Asia/Dubai, America/New_York
- **Languages:** en (English), hi (Hindi), ta (Tamil)

---

### 4. COMPANY_LOCATION (One-to-Many)

**Purpose:** Manages multiple physical locations/branches of a company with location-specific statutory details.

#### Schema Definition

| Column | Data Type | Length | Nullable | Default | Description |
|--------|-----------|--------|----------|---------|-------------|
| `id` | BIGINT | - | NOT NULL | AUTO | Primary Key |
| `company_id` | BIGINT | - | NOT NULL | - | Foreign Key to company.id |
| **Location Identity** |
| `type` | VARCHAR | 20 | NOT NULL | - | Location type (HO/BO/Factory) |
| `name` | VARCHAR | 100 | NOT NULL | - | Location name |
| `code` | VARCHAR | 20 | NULL | - | Location code |
| **Address Details** |
| `address_line1` | VARCHAR | 255 | NOT NULL | - | Address line 1 |
| `address_line2` | VARCHAR | 255 | NULL | - | Address line 2 |
| `state` | VARCHAR | 100 | NOT NULL | - | State/Province |
| `city` | VARCHAR | 100 | NOT NULL | - | City |
| `pincode` | VARCHAR | 10 | NOT NULL | - | Postal code |
| **Location Statutory Details** |
| `esi_number` | VARCHAR | 17 | NULL | - | Location-specific ESI number |
| `pf_number` | VARCHAR | 25 | NULL | - | Location-specific PF number |
| `pt_number` | VARCHAR | 30 | NULL | - | Location-specific PT number |
| `gstin` | VARCHAR | 15 | NULL | - | Location-specific GSTIN |
| `license_number` | VARCHAR | 50 | NULL | - | Factory/Shop license number |
| **Contact Information** |
| `contact_name` | VARCHAR | 100 | NOT NULL | - | Location contact person |
| `contact_phone` | VARCHAR | 15 | NOT NULL | - | Contact phone number |
| `contact_email` | VARCHAR | 100 | NULL | - | Contact email |
| **Status & Audit** |
| `is_active` | BOOLEAN | - | NULL | TRUE | Active status flag |
| `created_at` | TIMESTAMP | - | NULL | CURRENT | Creation timestamp |
| `updated_at` | TIMESTAMP | - | NULL | CURRENT | Last update timestamp |

#### Constraints
- **Primary Key:** `id`
- **Foreign Key:** `company_id` → `company(id)` with CASCADE DELETE

#### Business Rules
1. Multiple locations allowed per company
2. Location `type` values: 'Head Office', 'Branch Office', 'Factory', 'Warehouse', 'Regional Office'
3. Each location can have its own statutory numbers
4. At least one location is typically the head office
5. `is_active` allows soft deletion of locations

#### Analytics Queries
```sql
-- Count of locations per company
SELECT company_id, COUNT(*) as location_count
FROM company_location
WHERE is_active = true
GROUP BY company_id;

-- Locations by type
SELECT type, COUNT(*) as count
FROM company_location
WHERE is_active = true
GROUP BY type
ORDER BY count DESC;
```

---

### 5. COMPANY_BANK_ACCOUNT (One-to-Many)

**Purpose:** Manages multiple bank accounts for a company with primary account designation.

#### Schema Definition

| Column | Data Type | Length | Nullable | Default | Description |
|--------|-----------|--------|----------|---------|-------------|
| `id` | BIGINT | - | NOT NULL | AUTO | Primary Key |
| `company_id` | BIGINT | - | NOT NULL | - | Foreign Key to company.id |
| **Account Details** |
| `beneficiary_name` | VARCHAR | 200 | NOT NULL | - | Account beneficiary name |
| `account_name` | VARCHAR | 100 | NOT NULL | - | Account holder name |
| `bank_name` | VARCHAR | 100 | NOT NULL | - | Bank name |
| `branch_name` | VARCHAR | 100 | NULL | - | Branch name/location |
| `account_number` | VARCHAR | 18 | NOT NULL | - | Bank account number |
| `ifsc_code` | VARCHAR | 11 | NOT NULL | - | IFSC code |
| `account_type` | VARCHAR | 20 | NOT NULL | - | Account type |
| **Status Flags** |
| `is_primary` | BOOLEAN | - | NULL | FALSE | Primary account flag |
| `is_active` | BOOLEAN | - | NULL | TRUE | Active status flag |
| **Audit Fields** |
| `created_at` | TIMESTAMP | - | NULL | CURRENT | Creation timestamp |
| `updated_at` | TIMESTAMP | - | NULL | CURRENT | Last update timestamp |

#### Constraints
- **Primary Key:** `id`
- **Foreign Key:** `company_id` → `company(id)` with CASCADE DELETE
- **Unique Constraint:** `(company_id, account_number)` - Prevents duplicate accounts

#### Business Rules
1. Multiple bank accounts allowed per company
2. Only one account can be marked as `is_primary` per company
3. `account_type` values: 'Current', 'Savings', 'Overdraft', 'Cash Credit'
4. IFSC code format: 11 characters (AAAA0BBBBBB)
5. Account number: Maximum 18 digits
6. When setting a new primary account, existing primary is automatically unset

#### Analytics Queries
```sql
-- Count of bank accounts per company
SELECT company_id, COUNT(*) as account_count,
       SUM(CASE WHEN is_primary THEN 1 ELSE 0 END) as primary_count
FROM company_bank_account
WHERE is_active = true
GROUP BY company_id;

-- Bank account distribution by type
SELECT account_type, COUNT(*) as count
FROM company_bank_account
WHERE is_active = true
GROUP BY account_type;
```

---

## Business Logic & Rules

### Multi-Tenancy
- All companies are isolated by `tenant_id`
- Each tenant can have multiple companies
- Company codes must be unique globally and per tenant

### Data Integrity
1. **Referential Integrity:**
   - Child records automatically deleted when parent company is deleted (CASCADE)
   - Statutory and Settings: One-to-One enforced by unique constraint
   - Locations and Bank Accounts: One-to-Many with no upper limit

2. **Validation Rules:**
   - Email fields validated against email format
   - Phone numbers limited to 15 characters
   - Tax identifiers have specific format requirements
   - All monetary values stored as DECIMAL(10,2) or DECIMAL(5,2)

### Soft Delete Strategy
- `is_active` flag used for soft deletion
- Inactive records excluded from active queries
- Historical data preserved for audit

### Primary Account Logic
When setting a bank account as primary:
```java
// Business logic flow
1. Check if another account is already primary for the company
2. If yes, set existing primary account's is_primary = false
3. Set new account's is_primary = true
4. Only one primary account per company at any time
```

---

## API Endpoints

### GraphQL Queries

#### Company Queries
```graphql
# Get all companies
query {
  companies {
    id, name, code, industry, industryDescription
  }
}

# Get company by ID
query {
  company(id: 1) {
    id, name, statutory { pan, gstin }
  }
}

# Get companies by tenant
query {
  companiesByTenant(tenantId: "TENANT001") {
    id, name, locations { name, city }
  }
}
```

#### Statutory Queries
```graphql
query {
  companyStatutory(companyId: 1) {
    pan, tan, cin, gstin
    pfEnabled, pfEmployeeRate, pfEmployerRate
    esiEnabled, esiEmployeeRate, esiEmployerRate
  }
}
```

#### Location Queries
```graphql
query {
  companyLocations(companyId: 1) {
    id, name, type, city, state
    contactName, contactPhone
  }
}
```

#### Bank Account Queries
```graphql
query {
  companyBankAccounts(companyId: 1) {
    id, accountName, accountNumber, bankName
    isPrimary, isActive
  }
}
```

### GraphQL Mutations

#### Create Company
```graphql
mutation {
  createCompany(input: {
    tenantId: "TENANT001"
    code: "COMP001"
    name: "Acme Corporation"
    industry: "textile"
    industryDescription: "Organic cotton manufacturing"
    email: "contact@acme.com"
    isActive: true
  }) {
    id, name, code
  }
}
```

#### Update Statutory Details
```graphql
mutation {
  updateCompanyStatutory(
    companyId: 1,
    input: {
      pan: "AAAAA1234A"
      gstin: "27AAAAA1234A1Z5"
      pfEnabled: true
      pfEmployeeRate: 12.00
      pfEmployerRate: 12.00
    }
  ) {
    id, pan, gstin
  }
}
```

#### Create Location
```graphql
mutation {
  createCompanyLocation(
    companyId: 1,
    input: {
      type: "Branch Office"
      name: "Mumbai Branch"
      addressLine1: "123 Main Street"
      city: "Mumbai"
      state: "Maharashtra"
      pincode: "400001"
      contactName: "John Doe"
      contactPhone: "9876543210"
    }
  ) {
    id, name, type
  }
}
```

#### Create Bank Account
```graphql
mutation {
  createCompanyBankAccount(
    companyId: 1,
    input: {
      beneficiaryName: "Acme Corporation"
      accountName: "Acme Corp Ltd"
      bankName: "HDFC Bank"
      accountNumber: "123456789012"
      ifscCode: "HDFC0001234"
      accountType: "Current"
      isPrimary: true
    }
  ) {
    id, accountNumber, isPrimary
  }
}
```

---

## Analytics & Reporting

### Key Metrics

#### 1. Company Distribution Analytics
```sql
-- Companies by industry
SELECT industry, COUNT(*) as company_count
FROM company
WHERE is_active = true
GROUP BY industry
ORDER BY company_count DESC;

-- Companies by tenant
SELECT tenant_id, COUNT(*) as company_count
FROM company
WHERE is_active = true
GROUP BY tenant_id;

-- Active vs Inactive companies
SELECT
  is_active,
  COUNT(*) as count,
  ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM company
GROUP BY is_active;
```

#### 2. Statutory Compliance Analytics
```sql
-- PF/ESI/PT enablement statistics
SELECT
  COUNT(*) as total_companies,
  SUM(CASE WHEN pf_enabled THEN 1 ELSE 0 END) as pf_enabled_count,
  SUM(CASE WHEN esi_enabled THEN 1 ELSE 0 END) as esi_enabled_count,
  SUM(CASE WHEN pt_enabled THEN 1 ELSE 0 END) as pt_enabled_count
FROM company_statutory;

-- Companies missing tax identifiers
SELECT c.id, c.name,
  CASE WHEN s.pan IS NULL THEN 'Missing PAN' END as pan_status,
  CASE WHEN s.gstin IS NULL THEN 'Missing GSTIN' END as gstin_status
FROM company c
LEFT JOIN company_statutory s ON c.id = s.company_id
WHERE s.pan IS NULL OR s.gstin IS NULL;
```

#### 3. Location Analytics
```sql
-- Location distribution by type
SELECT type, COUNT(*) as count,
       AVG(CASE WHEN is_active THEN 1 ELSE 0 END) * 100 as active_percentage
FROM company_location
GROUP BY type;

-- Companies with multiple locations
SELECT company_id, COUNT(*) as location_count
FROM company_location
WHERE is_active = true
GROUP BY company_id
HAVING COUNT(*) > 1
ORDER BY location_count DESC;

-- State-wise location distribution
SELECT state, COUNT(*) as location_count
FROM company_location
WHERE is_active = true
GROUP BY state
ORDER BY location_count DESC;
```

#### 4. Banking Analytics
```sql
-- Companies with multiple bank accounts
SELECT company_id, COUNT(*) as account_count
FROM company_bank_account
WHERE is_active = true
GROUP BY company_id
HAVING COUNT(*) > 1;

-- Bank distribution
SELECT bank_name, COUNT(*) as account_count
FROM company_bank_account
WHERE is_active = true
GROUP BY bank_name
ORDER BY account_count DESC;

-- Account type distribution
SELECT account_type, COUNT(*) as count
FROM company_bank_account
WHERE is_active = true
GROUP BY account_type;
```

### Dashboard Queries

#### Company Setup Completion Dashboard
```sql
SELECT
  c.id,
  c.name,
  c.code,
  CASE WHEN s.id IS NOT NULL THEN true ELSE false END as has_statutory,
  CASE WHEN gs.id IS NOT NULL THEN true ELSE false END as has_settings,
  (SELECT COUNT(*) FROM company_location WHERE company_id = c.id AND is_active = true) as location_count,
  (SELECT COUNT(*) FROM company_bank_account WHERE company_id = c.id AND is_active = true) as bank_account_count,
  CASE
    WHEN s.id IS NOT NULL AND gs.id IS NOT NULL
         AND EXISTS (SELECT 1 FROM company_location WHERE company_id = c.id AND is_active = true)
         AND EXISTS (SELECT 1 FROM company_bank_account WHERE company_id = c.id AND is_active = true)
    THEN 'Complete'
    ELSE 'Incomplete'
  END as setup_status
FROM company c
LEFT JOIN company_statutory s ON c.id = s.company_id
LEFT JOIN company_general_settings gs ON c.id = gs.company_id
WHERE c.is_active = true;
```

---

## Data Flow Diagrams

### 1. Company Creation Flow

```
User Input (GraphQL)
        │
        ▼
┌────────────────────┐
│ Create Company     │
│ (Basic Info)       │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│ Company Table      │
│ - Insert Record    │
└─────────┬──────────┘
          │
          ├──────────────────┐
          │                  │
          ▼                  ▼
┌──────────────────┐  ┌──────────────────┐
│ Create Statutory │  │ Create Settings  │
│ (Default Values) │  │ (Default Values) │
└──────────────────┘  └──────────────────┘
```

### 2. Multi-Location Setup Flow

```
Company Exists
        │
        ▼
┌────────────────────┐
│ Add Location       │
│ - Type, Address    │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│ Validate           │
│ - Company exists   │
│ - Required fields  │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│ Location Table     │
│ - Insert Record    │
│ - Link to Company  │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│ Return Location    │
│ with Company Data  │
└────────────────────┘
```

### 3. Primary Bank Account Update Flow

```
Set Primary Request
        │
        ▼
┌────────────────────┐
│ Validate           │
│ - Account exists   │
│ - Belongs to Co.   │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│ Find Existing      │
│ Primary Account    │
└─────────┬──────────┘
          │
          ├──── Exists? ───┐
          │                │
          NO               YES
          │                │
          │                ▼
          │      ┌──────────────────┐
          │      │ Unset Existing   │
          │      │ is_primary=false │
          │      └────────┬─────────┘
          │               │
          └───────────────┘
                  │
                  ▼
        ┌──────────────────┐
        │ Set New Primary  │
        │ is_primary=true  │
        └──────────────────┘
```

---

## Best Practices

### 1. Data Entry
- Always provide complete company information
- Validate tax identifiers before saving
- Ensure at least one location and bank account
- Set proper organizational structure flags

### 2. Data Integrity
- Use transactions for multi-table operations
- Validate foreign key relationships
- Handle cascade deletes carefully
- Maintain audit trails (created_by, updated_by)

### 3. Performance Optimization
- Index frequently queried fields (tenant_id, code)
- Use pagination for large datasets
- Cache company settings for frequent access
- Optimize joins with proper foreign keys

### 4. Security
- Enforce tenant isolation at application level
- Validate user permissions before operations
- Sanitize inputs to prevent SQL injection
- Encrypt sensitive data (bank accounts, tax IDs)

---

## Migration & Maintenance

### Adding New Fields
```sql
-- Example: Adding a new field to company table
ALTER TABLE company ADD COLUMN registration_date DATE;

-- Update entity class
-- Update DTOs
-- Update GraphQL schema
-- Update mappers
```

### Data Migration Scripts
```sql
-- Example: Backfill industry_description from industry
UPDATE company
SET industry_description =
  CASE
    WHEN industry = 'textile' THEN 'Textile and Garment Manufacturing'
    WHEN industry = 'it-services' THEN 'Information Technology Services'
    ELSE 'General Business'
  END
WHERE industry_description IS NULL;
```

---

## Troubleshooting

### Common Issues

**Issue 1: Duplicate Company Code**
```sql
-- Check for duplicates
SELECT code, COUNT(*)
FROM company
GROUP BY code
HAVING COUNT(*) > 1;
```

**Issue 2: Missing Statutory/Settings**
```sql
-- Find companies without statutory
SELECT c.id, c.name
FROM company c
LEFT JOIN company_statutory s ON c.id = s.company_id
WHERE s.id IS NULL;
```

**Issue 3: Multiple Primary Bank Accounts**
```sql
-- Find companies with multiple primary accounts
SELECT company_id, COUNT(*)
FROM company_bank_account
WHERE is_primary = true
GROUP BY company_id
HAVING COUNT(*) > 1;

-- Fix: Keep only the latest
UPDATE company_bank_account SET is_primary = false
WHERE company_id = ? AND id != ?;
```

---

## Appendix

### Industry Categories
- Textile
- IT Services
- Manufacturing
- Retail
- Healthcare
- Education
- Finance
- Construction
- Hospitality
- Agriculture

### Company Types
- Private Limited
- Public Limited
- Partnership
- LLP (Limited Liability Partnership)
- Sole Proprietorship
- OPC (One Person Company)

### Location Types
- Head Office (HO)
- Branch Office (BO)
- Factory
- Warehouse
- Regional Office
- Sales Office

### Account Types
- Current Account
- Savings Account
- Overdraft Account
- Cash Credit Account

---

**Document End**

For questions or clarifications, contact: HRMS Development Team
