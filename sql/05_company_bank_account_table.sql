-- ============================================
-- Company Setup Module - Database Schema
-- Table 5: Company Bank Accounts
-- ============================================

-- Drop table if exists
DROP TABLE IF EXISTS company_bank_account CASCADE;

-- Create Company Bank Account table
CREATE TABLE company_bank_account (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,

    -- Account Details
    beneficiary_name VARCHAR(200) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    branch_name VARCHAR(100),
    account_number VARCHAR(18) NOT NULL,
    ifsc_code VARCHAR(11) NOT NULL CHECK (ifsc_code ~ '^[A-Z]{4}0[A-Z0-9]{6}$'),
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('Current Account', 'Cash Credit', 'Overdraft')),

    -- Flags
    is_primary BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key
    CONSTRAINT fk_bank_account_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,

    -- Unique constraints
    CONSTRAINT uk_bank_account_company_number UNIQUE (company_id, account_number)
);

-- Create indexes
CREATE INDEX idx_bank_account_company ON company_bank_account(company_id);
CREATE INDEX idx_bank_account_primary ON company_bank_account(is_primary);
CREATE INDEX idx_bank_account_active ON company_bank_account(is_active);

-- Add comments
COMMENT ON TABLE company_bank_account IS 'Company bank accounts for payroll and transactions';
COMMENT ON COLUMN company_bank_account.beneficiary_name IS 'Name as per bank account';
COMMENT ON COLUMN company_bank_account.account_number IS 'Bank account number';
COMMENT ON COLUMN company_bank_account.ifsc_code IS 'Indian Financial System Code (11 chars)';
COMMENT ON COLUMN company_bank_account.is_primary IS 'Primary account for salary payments';
COMMENT ON COLUMN company_bank_account.account_type IS 'Account type: Current Account, Cash Credit, Overdraft';
