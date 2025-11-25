-- ============================================
-- Company Setup Module - Database Schema
-- Table 1: Company (Main Table)
-- ============================================

-- Drop table if exists (for clean recreation)
DROP TABLE IF EXISTS company CASCADE;

-- Create Company table
CREATE TABLE company (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(50),
    industry VARCHAR(50),
    company_type VARCHAR(20),
    logo VARCHAR(255),

    -- Address
    address_line1 TEXT,
    address_line2 TEXT,
    country VARCHAR(50),
    state VARCHAR(50),
    city VARCHAR(50),
    pincode VARCHAR(10),

    -- Contact Info
    primary_phone VARCHAR(15),
    alternate_phone VARCHAR(15),
    email VARCHAR(100),
    website VARCHAR(100),

    -- Point of Contact
    contact_name VARCHAR(100),
    contact_designation VARCHAR(50),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(15),

    -- Other
    admin_notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,

    -- Audit fields
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT uk_company_tenant_code UNIQUE (tenant_id, code),
    CONSTRAINT uk_company_tenant_name UNIQUE (tenant_id, name)
);

-- Create indexes for better query performance
CREATE INDEX idx_company_tenant ON company(tenant_id);
CREATE INDEX idx_company_code ON company(code);
CREATE INDEX idx_company_name ON company(name);
CREATE INDEX idx_company_active ON company(is_active);

-- Add comments for documentation
COMMENT ON TABLE company IS 'Main company/organization master table';
COMMENT ON COLUMN company.tenant_id IS 'Multi-tenant identifier';
COMMENT ON COLUMN company.code IS 'Unique company code';
COMMENT ON COLUMN company.name IS 'Official company name';
COMMENT ON COLUMN company.short_name IS 'Short name or abbreviation';
COMMENT ON COLUMN company.company_type IS 'Type: Private Limited, Public Limited, LLP, etc.';
