-- Database Migration for State and Country Management Modules
-- Execute this script manually after review

-- Drop existing states table if exists
DROP TABLE IF EXISTS states CASCADE;

-- Create countries table
CREATE TABLE countries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    currency_code VARCHAR(10),
    phone_code VARCHAR(10),
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    CONSTRAINT uk_countries_tenant_code UNIQUE (tenant_id, code),
    CONSTRAINT uk_countries_tenant_name UNIQUE (tenant_id, name)
);

-- Create functional indexes for case-insensitive uniqueness on countries
CREATE UNIQUE INDEX idx_countries_tenant_code_lower ON countries (tenant_id, LOWER(code));
CREATE UNIQUE INDEX idx_countries_tenant_name_lower ON countries (tenant_id, LOWER(name));

-- Create index on active countries
CREATE INDEX idx_countries_active ON countries (is_active);
CREATE INDEX idx_countries_tenant_active ON countries (tenant_id, is_active);

-- Create states table
CREATE TABLE states (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    country_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    state_code VARCHAR(10),
    is_union_territory BOOLEAN NOT NULL DEFAULT false,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP,
    CONSTRAINT fk_states_country FOREIGN KEY (country_id) REFERENCES countries(id) ON DELETE CASCADE,
    CONSTRAINT uk_states_tenant_country_code UNIQUE (tenant_id, country_id, code),
    CONSTRAINT uk_states_tenant_country_name UNIQUE (tenant_id, country_id, name)
);

-- Create functional indexes for case-insensitive uniqueness on states
CREATE UNIQUE INDEX idx_states_tenant_country_code_lower ON states (tenant_id, country_id, LOWER(code));
CREATE UNIQUE INDEX idx_states_tenant_country_name_lower ON states (tenant_id, country_id, LOWER(name));

-- Create index on active states
CREATE INDEX idx_states_active ON states (is_active);
CREATE INDEX idx_states_tenant_active ON states (tenant_id, is_active);
CREATE INDEX idx_states_country ON states (country_id);
CREATE INDEX idx_states_tenant_country ON states (tenant_id, country_id);

-- Add comments for documentation
COMMENT ON TABLE countries IS 'Master table for country data with multi-tenancy support';
COMMENT ON TABLE states IS 'Master table for state/province data with country relationship';

COMMENT ON COLUMN states.is_union_territory IS 'Flag to indicate if the state is a union territory (mainly for India)';
COMMENT ON COLUMN states.state_code IS 'Optional state-specific code (e.g., GST state code)';
