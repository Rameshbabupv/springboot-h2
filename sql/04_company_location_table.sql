-- ============================================
-- Company Setup Module - Database Schema
-- Table 4: Company Locations
-- ============================================

-- Drop table if exists
DROP TABLE IF EXISTS company_location CASCADE;

-- Create Company Location table
CREATE TABLE company_location (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,

    -- Basic Information
    type VARCHAR(20) NOT NULL CHECK (type IN ('Head Office', 'Branch', 'Factory', 'Warehouse', 'Store')),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20),

    -- Address
    address_line1 TEXT NOT NULL,
    address_line2 TEXT,
    state VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,

    -- Location-Specific Statutory Registration Numbers
    esi_number VARCHAR(17),
    pf_number VARCHAR(25),
    pt_number VARCHAR(30),
    gstin VARCHAR(15),
    license_number VARCHAR(50),

    -- Site Contact Person
    contact_name VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(15) NOT NULL,
    contact_email VARCHAR(100),

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key
    CONSTRAINT fk_location_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,

    -- Unique constraint
    CONSTRAINT uk_location_company_code UNIQUE (company_id, code)
);

-- Create indexes
CREATE INDEX idx_location_company ON company_location(company_id);
CREATE INDEX idx_location_type ON company_location(type);
CREATE INDEX idx_location_active ON company_location(is_active);
CREATE INDEX idx_location_city ON company_location(city);

-- Add comments
COMMENT ON TABLE company_location IS 'Company locations/branches/sites';
COMMENT ON COLUMN company_location.type IS 'Location type: Head Office, Branch, Factory, Warehouse, Store';
COMMENT ON COLUMN company_location.code IS 'Unique location code within company';
COMMENT ON COLUMN company_location.esi_number IS 'Location-specific ESI number';
COMMENT ON COLUMN company_location.pf_number IS 'Location-specific PF number';
