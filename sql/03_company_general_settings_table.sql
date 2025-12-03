-- ============================================
-- Company Setup Module - Database Schema
-- Table 3: Company General Settings
-- ============================================

-- Drop table if exists
DROP TABLE IF EXISTS company_general_settings CASCADE;

-- Create Company General Settings table
CREATE TABLE company_general_settings (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,

    -- Organizational Structure Flags
    enable_divisions BOOLEAN DEFAULT FALSE,
    enable_department BOOLEAN DEFAULT TRUE,
    enable_section BOOLEAN DEFAULT FALSE,
    enable_grade BOOLEAN DEFAULT FALSE,

    -- System Configuration
    currency VARCHAR(3) DEFAULT 'INR',
    date_format VARCHAR(15) DEFAULT 'DD/MM/YYYY',
    time_zone VARCHAR(50) DEFAULT 'Asia/Kolkata',
    financial_year_start VARCHAR(15) DEFAULT 'April',
    language VARCHAR(5) DEFAULT 'en',

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key
    CONSTRAINT fk_settings_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,

    -- Unique constraint - one settings record per company
    CONSTRAINT uk_settings_company UNIQUE (company_id)
);

-- Create indexes
CREATE INDEX idx_settings_company ON company_general_settings(company_id);

-- Add comments
COMMENT ON TABLE company_general_settings IS 'Company organizational structure and system settings';
COMMENT ON COLUMN company_general_settings.enable_divisions IS 'Enable divisions in org hierarchy';
COMMENT ON COLUMN company_general_settings.enable_department IS 'Enable departments in org hierarchy';
COMMENT ON COLUMN company_general_settings.enable_section IS 'Enable sections in org hierarchy';
COMMENT ON COLUMN company_general_settings.enable_grade IS 'Enable employee grades';
COMMENT ON COLUMN company_general_settings.currency IS 'Base currency code (ISO 4217)';
COMMENT ON COLUMN company_general_settings.financial_year_start IS 'Month when financial year starts';
