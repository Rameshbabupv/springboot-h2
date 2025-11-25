-- ============================================
-- Company Setup Module - Database Schema
-- Table 2: Company Statutory Details
-- ============================================

-- Drop table if exists
DROP TABLE IF EXISTS company_statutory CASCADE;

-- Create Company Statutory table
CREATE TABLE company_statutory (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,

    -- Tax Identification Numbers
    pan VARCHAR(10) CHECK (pan ~ '^[A-Z]{5}[0-9]{4}[A-Z]$'),
    tan VARCHAR(10) CHECK (tan ~ '^[A-Z]{4}[0-9]{5}[A-Z]$'),
    cin VARCHAR(21),
    lin VARCHAR(21),
    gstin VARCHAR(15),

    -- Provident Fund (PF) Configuration
    pf_enabled BOOLEAN DEFAULT FALSE,
    pf_account_number VARCHAR(25),
    pf_ceiling DECIMAL(10,2) DEFAULT 15000,
    pf_employee_rate DECIMAL(5,2) DEFAULT 12.00,
    pf_employer_rate DECIMAL(5,2) DEFAULT 12.00,
    pf_employer_epf_rate DECIMAL(5,2) DEFAULT 3.67,
    pf_employer_eps_rate DECIMAL(5,2) DEFAULT 8.33,

    -- Employee State Insurance (ESI) Configuration
    esi_enabled BOOLEAN DEFAULT FALSE,
    esi_number VARCHAR(17),
    esi_ceiling DECIMAL(10,2) DEFAULT 21000,
    esi_employee_rate DECIMAL(5,2) DEFAULT 0.75,
    esi_employer_rate DECIMAL(5,2) DEFAULT 3.25,

    -- Professional Tax (PT) Configuration
    pt_enabled BOOLEAN DEFAULT FALSE,
    pt_state VARCHAR(50),
    pt_registration_number VARCHAR(30),
    pt_registration_date DATE,
    pt_valid_upto DATE,

    -- HR Policies
    retirement_age INTEGER DEFAULT 60,

    -- TDS Configuration
    tds_type VARCHAR(10) DEFAULT 'TDS',
    allow_tds_override BOOLEAN DEFAULT FALSE,

    -- Applicable Acts (stored as comma-separated)
    applicable_acts TEXT,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key
    CONSTRAINT fk_statutory_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,

    -- Unique constraint - one statutory record per company
    CONSTRAINT uk_statutory_company UNIQUE (company_id)
);

-- Create indexes
CREATE INDEX idx_statutory_company ON company_statutory(company_id);
CREATE INDEX idx_statutory_pan ON company_statutory(pan);
CREATE INDEX idx_statutory_gstin ON company_statutory(gstin);

-- Add comments
COMMENT ON TABLE company_statutory IS 'Company statutory and compliance details';
COMMENT ON COLUMN company_statutory.pan IS 'Permanent Account Number (10 chars)';
COMMENT ON COLUMN company_statutory.tan IS 'Tax Deduction Account Number';
COMMENT ON COLUMN company_statutory.cin IS 'Corporate Identity Number';
COMMENT ON COLUMN company_statutory.gstin IS 'Goods and Services Tax Identification Number';
