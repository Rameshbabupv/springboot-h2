# Company Setup Module - Database Schema Reference

**Version:** 1.0
**Database:** PostgreSQL
**Last Updated:** November 26, 2025

---

## Quick Reference

### Table Summary

| Table | Records | Relationship | Purpose |
|-------|---------|--------------|---------|
| company | 1:N per tenant | Parent | Core company data |
| company_statutory | 1:1 with company | Child | Tax & compliance |
| company_general_settings | 1:1 with company | Child | Organization settings |
| company_location | N:1 with company | Child | Physical locations |
| company_bank_account | N:1 with company | Child | Bank accounts |

---

## DDL Scripts

### 1. CREATE TABLE: company

```sql
CREATE TABLE company (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,

    -- Multi-tenancy & Identity
    tenant_id VARCHAR(50) NOT NULL,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(10),

    -- Industry Information
    industry VARCHAR(50),
    industry_description VARCHAR(255),
    company_type VARCHAR(50),
    logo TEXT,

    -- Registered Address
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    country VARCHAR(100) DEFAULT 'India',
    state VARCHAR(100),
    city VARCHAR(100),
    pincode VARCHAR(10),

    -- Contact Information
    primary_phone VARCHAR(15),
    alternate_phone VARCHAR(15),
    email VARCHAR(100),
    website VARCHAR(200),

    -- Primary Contact Person
    contact_name VARCHAR(100),
    contact_designation VARCHAR(100),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(15),

    -- Administrative
    admin_notes VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,

    -- Audit Fields
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT uk_company_code UNIQUE (code),
    CONSTRAINT uk_company_tenant_code UNIQUE (tenant_id, code)
);

-- Indexes
CREATE INDEX idx_company_tenant ON company(tenant_id);
CREATE INDEX idx_company_active ON company(is_active);
CREATE INDEX idx_company_industry ON company(industry);
```

### 2. CREATE TABLE: company_statutory

```sql
CREATE TABLE company_statutory (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,

    -- Tax Identifiers
    pan VARCHAR(10),              -- Format: AAAAA9999A
    tan VARCHAR(10),              -- Format: AAAA99999A
    cin VARCHAR(21),              -- 21 characters
    lin VARCHAR(21),              -- 21 characters
    gstin VARCHAR(15),            -- Format: 27AAAAA1234A1Z5

    -- PF (Provident Fund) Configuration
    pf_enabled BOOLEAN DEFAULT FALSE,
    pf_account_number VARCHAR(25),
    pf_ceiling DECIMAL(10,2),
    pf_employee_rate DECIMAL(5,2),
    pf_employer_rate DECIMAL(5,2),
    pf_employer_epf_rate DECIMAL(5,2),
    pf_employer_eps_rate DECIMAL(5,2),

    -- ESI (Employee State Insurance) Configuration
    esi_enabled BOOLEAN DEFAULT FALSE,
    esi_number VARCHAR(17),
    esi_ceiling DECIMAL(10,2),
    esi_employee_rate DECIMAL(5,2),
    esi_employer_rate DECIMAL(5,2),

    -- PT (Professional Tax) Configuration
    pt_enabled BOOLEAN DEFAULT FALSE,
    pt_state VARCHAR(50),
    pt_registration_number VARCHAR(30),
    pt_registration_date DATE,
    pt_valid_upto DATE,

    -- HR & TDS Configuration
    retirement_age INTEGER DEFAULT 58,
    tds_type VARCHAR(10),
    allow_tds_override BOOLEAN DEFAULT FALSE,
    applicable_acts TEXT,          -- JSON format

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_statutory_company FOREIGN KEY (company_id)
        REFERENCES company(id) ON DELETE CASCADE,
    CONSTRAINT uk_statutory_company UNIQUE (company_id)
);

-- Indexes
CREATE INDEX idx_statutory_company ON company_statutory(company_id);
CREATE INDEX idx_statutory_pan ON company_statutory(pan);
CREATE INDEX idx_statutory_gstin ON company_statutory(gstin);
```

### 3. CREATE TABLE: company_general_settings

```sql
CREATE TABLE company_general_settings (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,

    -- Organizational Structure Toggles
    enable_divisions BOOLEAN DEFAULT TRUE,
    enable_department BOOLEAN DEFAULT TRUE,
    enable_section BOOLEAN DEFAULT TRUE,
    enable_grade BOOLEAN DEFAULT TRUE,

    -- System Configuration
    currency VARCHAR(3) DEFAULT 'INR',
    date_format VARCHAR(15) DEFAULT 'DD-MM-YYYY',
    time_zone VARCHAR(50) DEFAULT 'Asia/Kolkata',
    financial_year_start VARCHAR(15) DEFAULT 'April',
    language VARCHAR(5) DEFAULT 'en',

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_settings_company FOREIGN KEY (company_id)
        REFERENCES company(id) ON DELETE CASCADE,
    CONSTRAINT uk_settings_company UNIQUE (company_id)
);

-- Indexes
CREATE INDEX idx_settings_company ON company_general_settings(company_id);
```

### 4. CREATE TABLE: company_location

```sql
CREATE TABLE company_location (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,

    -- Location Identity
    type VARCHAR(20) NOT NULL,    -- HO, BO, Factory, etc.
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20),

    -- Address Details
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    state VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,

    -- Location-Specific Statutory
    esi_number VARCHAR(17),
    pf_number VARCHAR(25),
    pt_number VARCHAR(30),
    gstin VARCHAR(15),
    license_number VARCHAR(50),

    -- Contact Information
    contact_name VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(15) NOT NULL,
    contact_email VARCHAR(100),

    -- Status & Audit
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_location_company FOREIGN KEY (company_id)
        REFERENCES company(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_location_company ON company_location(company_id);
CREATE INDEX idx_location_type ON company_location(type);
CREATE INDEX idx_location_active ON company_location(is_active);
CREATE INDEX idx_location_state ON company_location(state);
```

### 5. CREATE TABLE: company_bank_account

```sql
CREATE TABLE company_bank_account (
    -- Primary Key
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,

    -- Account Details
    beneficiary_name VARCHAR(200) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    branch_name VARCHAR(100),
    account_number VARCHAR(18) NOT NULL,
    ifsc_code VARCHAR(11) NOT NULL,
    account_type VARCHAR(20) NOT NULL,

    -- Status Flags
    is_primary BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_bank_account_company FOREIGN KEY (company_id)
        REFERENCES company(id) ON DELETE CASCADE,
    CONSTRAINT uk_bank_account_number UNIQUE (company_id, account_number)
);

-- Indexes
CREATE INDEX idx_bank_company ON company_bank_account(company_id);
CREATE INDEX idx_bank_primary ON company_bank_account(is_primary);
CREATE INDEX idx_bank_active ON company_bank_account(is_active);
CREATE INDEX idx_bank_name ON company_bank_account(bank_name);
```

---

## Sample Data

### Insert Sample Company

```sql
-- 1. Insert Company
INSERT INTO company (tenant_id, code, name, short_name, industry, industry_description,
                     company_type, email, primary_phone, country, is_active)
VALUES ('TENANT001', 'ACME001', 'Acme Corporation', 'ACME',
        'textile', 'Organic cotton textile manufacturing and export',
        'Private Limited', 'contact@acme.com', '9876543210', 'India', TRUE)
RETURNING id;  -- Returns: 1

-- 2. Insert Statutory Details
INSERT INTO company_statutory (company_id, pan, tan, gstin,
                               pf_enabled, pf_employee_rate, pf_employer_rate,
                               esi_enabled, esi_employee_rate, esi_employer_rate,
                               retirement_age)
VALUES (1, 'AAAAA1234A', 'AAAA12345A', '27AAAAA1234A1Z5',
        TRUE, 12.00, 12.00,
        TRUE, 0.75, 3.25,
        58);

-- 3. Insert General Settings
INSERT INTO company_general_settings (company_id, enable_divisions, enable_department,
                                     currency, date_format, time_zone, financial_year_start)
VALUES (1, TRUE, TRUE, 'INR', 'DD-MM-YYYY', 'Asia/Kolkata', 'April');

-- 4. Insert Head Office Location
INSERT INTO company_location (company_id, type, name, address_line1, city, state, pincode,
                              contact_name, contact_phone, is_active)
VALUES (1, 'Head Office', 'Acme HQ', '123 Main Street', 'Mumbai', 'Maharashtra', '400001',
        'John Doe', '9876543210', TRUE);

-- 5. Insert Primary Bank Account
INSERT INTO company_bank_account (company_id, beneficiary_name, account_name, bank_name,
                                 account_number, ifsc_code, account_type, is_primary, is_active)
VALUES (1, 'Acme Corporation', 'Acme Corp Ltd', 'HDFC Bank',
        '123456789012', 'HDFC0001234', 'Current', TRUE, TRUE);
```

---

## Data Validation Queries

### 1. Check Referential Integrity

```sql
-- Find companies without statutory
SELECT c.id, c.name, c.code
FROM company c
LEFT JOIN company_statutory s ON c.id = s.company_id
WHERE s.id IS NULL;

-- Find companies without settings
SELECT c.id, c.name, c.code
FROM company c
LEFT JOIN company_general_settings gs ON c.id = gs.company_id
WHERE gs.id IS NULL;

-- Find companies without locations
SELECT c.id, c.name, c.code
FROM company c
WHERE NOT EXISTS (
    SELECT 1 FROM company_location l
    WHERE l.company_id = c.id AND l.is_active = TRUE
);

-- Find companies without bank accounts
SELECT c.id, c.name, c.code
FROM company c
WHERE NOT EXISTS (
    SELECT 1 FROM company_bank_account b
    WHERE b.company_id = c.id AND b.is_active = TRUE
);
```

### 2. Data Quality Checks

```sql
-- Check for invalid PAN format (should be 10 chars)
SELECT company_id, pan
FROM company_statutory
WHERE pan IS NOT NULL AND LENGTH(pan) != 10;

-- Check for invalid GSTIN format (should be 15 chars)
SELECT company_id, gstin
FROM company_statutory
WHERE gstin IS NOT NULL AND LENGTH(gstin) != 15;

-- Check for invalid IFSC format (should be 11 chars)
SELECT company_id, ifsc_code
FROM company_bank_account
WHERE LENGTH(ifsc_code) != 11;

-- Check for companies with multiple primary bank accounts (should be max 1)
SELECT company_id, COUNT(*) as primary_count
FROM company_bank_account
WHERE is_primary = TRUE
GROUP BY company_id
HAVING COUNT(*) > 1;
```

### 3. Audit & Compliance Queries

```sql
-- Companies created in last 30 days
SELECT id, name, code, created_at
FROM company
WHERE created_at >= CURRENT_DATE - INTERVAL '30 days'
ORDER BY created_at DESC;

-- Companies without statutory compliance setup
SELECT c.id, c.name,
       CASE WHEN s.pf_enabled THEN 'Yes' ELSE 'No' END as pf_configured,
       CASE WHEN s.esi_enabled THEN 'Yes' ELSE 'No' END as esi_configured,
       CASE WHEN s.pt_enabled THEN 'Yes' ELSE 'No' END as pt_configured
FROM company c
LEFT JOIN company_statutory s ON c.id = s.company_id
WHERE s.pf_enabled = FALSE AND s.esi_enabled = FALSE;
```

---

## Performance Optimization

### Recommended Indexes

```sql
-- Company table indexes
CREATE INDEX idx_company_tenant_active ON company(tenant_id, is_active);
CREATE INDEX idx_company_created_at ON company(created_at DESC);

-- Location table indexes
CREATE INDEX idx_location_company_active ON company_location(company_id, is_active);
CREATE INDEX idx_location_state_city ON company_location(state, city);

-- Bank account indexes
CREATE INDEX idx_bank_company_primary ON company_bank_account(company_id, is_primary);
```

### Query Optimization Tips

```sql
-- Use EXPLAIN ANALYZE to check query performance
EXPLAIN ANALYZE
SELECT c.*, s.pan, s.gstin, COUNT(l.id) as location_count
FROM company c
LEFT JOIN company_statutory s ON c.id = s.company_id
LEFT JOIN company_location l ON c.id = l.company_id AND l.is_active = TRUE
WHERE c.tenant_id = 'TENANT001' AND c.is_active = TRUE
GROUP BY c.id, s.id;

-- Use pagination for large result sets
SELECT * FROM company
WHERE tenant_id = 'TENANT001'
ORDER BY id
LIMIT 20 OFFSET 0;
```

---

## Maintenance Scripts

### Data Cleanup

```sql
-- Soft delete inactive locations older than 1 year
UPDATE company_location
SET is_active = FALSE
WHERE is_active = TRUE
  AND updated_at < CURRENT_DATE - INTERVAL '1 year'
  AND id NOT IN (SELECT company_id FROM employees);

-- Archive old inactive companies (hard delete after backup)
-- CAUTION: Ensure backup before running
DELETE FROM company
WHERE is_active = FALSE
  AND updated_at < CURRENT_DATE - INTERVAL '2 years'
  AND id NOT IN (SELECT company_id FROM employees);
```

### Backup Strategies

```sql
-- Backup company data
COPY (SELECT * FROM company WHERE tenant_id = 'TENANT001')
TO '/backup/company_tenant001.csv' WITH CSV HEADER;

-- Restore from backup
COPY company FROM '/backup/company_tenant001.csv' WITH CSV HEADER;
```

---

## Migration Examples

### Add New Column

```sql
-- Step 1: Add column
ALTER TABLE company ADD COLUMN registration_date DATE;

-- Step 2: Update existing records with default
UPDATE company SET registration_date = created_at::date
WHERE registration_date IS NULL;

-- Step 3: Add constraint if needed
ALTER TABLE company ALTER COLUMN registration_date SET NOT NULL;
```

### Modify Column Type

```sql
-- Increase column size
ALTER TABLE company ALTER COLUMN short_name TYPE VARCHAR(20);

-- Change decimal precision
ALTER TABLE company_statutory
ALTER COLUMN pf_employee_rate TYPE DECIMAL(6,3);
```

### Add Foreign Key Constraint

```sql
-- Add FK to existing table
ALTER TABLE company_location
ADD CONSTRAINT fk_location_state
FOREIGN KEY (state) REFERENCES states(name);
```

---

## Common SQL Patterns

### 1. Complete Company Setup View

```sql
CREATE OR REPLACE VIEW v_company_complete AS
SELECT
    c.id,
    c.tenant_id,
    c.code,
    c.name,
    c.short_name,
    c.industry,
    c.industry_description,
    c.is_active,

    -- Statutory info
    s.pan,
    s.gstin,
    s.pf_enabled,
    s.esi_enabled,

    -- Settings
    gs.currency,
    gs.date_format,

    -- Aggregated counts
    (SELECT COUNT(*) FROM company_location WHERE company_id = c.id AND is_active = TRUE) as location_count,
    (SELECT COUNT(*) FROM company_bank_account WHERE company_id = c.id AND is_active = TRUE) as bank_account_count,

    -- Timestamps
    c.created_at,
    c.updated_at
FROM company c
LEFT JOIN company_statutory s ON c.id = s.company_id
LEFT JOIN company_general_settings gs ON c.id = gs.company_id;
```

### 2. Setup Completion Status

```sql
SELECT
    c.id,
    c.name,
    CASE
        WHEN s.id IS NULL THEN 'Missing Statutory'
        WHEN gs.id IS NULL THEN 'Missing Settings'
        WHEN NOT EXISTS (SELECT 1 FROM company_location WHERE company_id = c.id)
            THEN 'Missing Location'
        WHEN NOT EXISTS (SELECT 1 FROM company_bank_account WHERE company_id = c.id)
            THEN 'Missing Bank Account'
        ELSE 'Complete'
    END as setup_status
FROM company c
LEFT JOIN company_statutory s ON c.id = s.company_id
LEFT JOIN company_general_settings gs ON c.id = gs.company_id;
```

---

## Constraints Summary

### Primary Keys
- All tables use BIGSERIAL auto-increment primary keys
- Format: `id BIGSERIAL PRIMARY KEY`

### Foreign Keys
```sql
company_statutory.company_id      → company.id (CASCADE DELETE)
company_general_settings.company_id → company.id (CASCADE DELETE)
company_location.company_id       → company.id (CASCADE DELETE)
company_bank_account.company_id   → company.id (CASCADE DELETE)
```

### Unique Constraints
```sql
company: code (global), (tenant_id, code) (per tenant)
company_statutory: company_id (one-to-one)
company_general_settings: company_id (one-to-one)
company_bank_account: (company_id, account_number)
```

---

## Field Length Reference

### VARCHAR Fields

| Field | Max Length | Format/Example |
|-------|------------|----------------|
| tenant_id | 50 | TENANT001 |
| company.code | 20 | COMP001 |
| company.name | 200 | Full company name |
| short_name | 10 | ACME |
| industry | 50 | textile, it-services |
| industry_description | 255 | Free text |
| pan | 10 | AAAAA9999A |
| tan | 10 | AAAA99999A |
| cin | 21 | U12345MH2020PTC123456 |
| gstin | 15 | 27AAAAA1234A1Z5 |
| ifsc_code | 11 | HDFC0001234 |
| account_number | 18 | Max 18 digits |
| phone | 15 | +91-9876543210 |
| email | 100 | Standard email |

---

**Document End**

For schema modifications, contact: Database Team
For application changes, contact: Development Team
