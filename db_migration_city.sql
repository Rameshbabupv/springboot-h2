-- ============================================================================
-- City Management Module - Database Migration Script
-- ============================================================================
-- Description: Complete overhaul of cities table with dual parent relationships
--              (City -> State -> Country) following State entity patterns
-- Version: 1.0
-- Date: 2025-11-26
-- ============================================================================

-- Drop existing cities table
DROP TABLE IF EXISTS cities CASCADE;

-- Create cities table with proper foreign keys and constraints
CREATE TABLE cities (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    country_id BIGINT NOT NULL,
    state_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    pincode VARCHAR(20),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_city_country FOREIGN KEY (country_id)
        REFERENCES countries(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_city_state FOREIGN KEY (state_id)
        REFERENCES states(id) ON DELETE RESTRICT ON UPDATE CASCADE,

    -- Unique constraints per tenant+state+country
    CONSTRAINT uq_city_code UNIQUE (tenant_id, state_id, country_id, code),
    CONSTRAINT uq_city_name UNIQUE (tenant_id, state_id, country_id, name),

    -- Check constraints for latitude and longitude
    CONSTRAINT chk_city_latitude CHECK (latitude >= -90 AND latitude <= 90),
    CONSTRAINT chk_city_longitude CHECK (longitude >= -180 AND longitude <= 180)
);

-- ============================================================================
-- INDEXES
-- ============================================================================

-- Primary lookup indexes
CREATE INDEX idx_city_tenant ON cities(tenant_id);
CREATE INDEX idx_city_state ON cities(state_id);
CREATE INDEX idx_city_country ON cities(country_id);

-- Composite indexes for cascading dropdowns and filtering
CREATE INDEX idx_city_tenant_state ON cities(tenant_id, state_id);
CREATE INDEX idx_city_tenant_country ON cities(tenant_id, country_id);
CREATE INDEX idx_city_state_country ON cities(state_id, country_id);

-- Active cities filter (most common query)
CREATE INDEX idx_city_active ON cities(is_active) WHERE is_active = true;
CREATE INDEX idx_city_tenant_active ON cities(tenant_id, is_active) WHERE is_active = true;

-- Case-insensitive search indexes (for name and code)
CREATE INDEX idx_city_name_lower ON cities(tenant_id, state_id, country_id, LOWER(name));
CREATE INDEX idx_city_code_upper ON cities(tenant_id, state_id, country_id, UPPER(code));

-- Search by pincode (common use case)
CREATE INDEX idx_city_pincode ON cities(pincode) WHERE pincode IS NOT NULL;

-- Audit trail indexes
CREATE INDEX idx_city_created_at ON cities(created_at);
CREATE INDEX idx_city_updated_at ON cities(updated_at);

-- ============================================================================
-- COMMENTS
-- ============================================================================

COMMENT ON TABLE cities IS 'City master data with dual parent relationships to State and Country';
COMMENT ON COLUMN cities.id IS 'Primary key';
COMMENT ON COLUMN cities.tenant_id IS 'Multi-tenant identifier (max 50 chars)';
COMMENT ON COLUMN cities.country_id IS 'Foreign key to countries table';
COMMENT ON COLUMN cities.state_id IS 'Foreign key to states table';
COMMENT ON COLUMN cities.name IS 'City name (max 100 chars, unique per tenant+state+country)';
COMMENT ON COLUMN cities.code IS 'City code (max 10 chars, unique per tenant+state+country, auto-uppercase)';
COMMENT ON COLUMN cities.pincode IS 'Postal code/PIN code (max 20 chars)';
COMMENT ON COLUMN cities.latitude IS 'Latitude coordinate (-90 to 90, precision 10,7)';
COMMENT ON COLUMN cities.longitude IS 'Longitude coordinate (-180 to 180, precision 10,7)';
COMMENT ON COLUMN cities.description IS 'Optional description (max 500 chars)';
COMMENT ON COLUMN cities.is_active IS 'Active status flag (default true)';
COMMENT ON COLUMN cities.created_by IS 'User who created this record';
COMMENT ON COLUMN cities.created_at IS 'Timestamp when record was created';
COMMENT ON COLUMN cities.updated_by IS 'User who last updated this record';
COMMENT ON COLUMN cities.updated_at IS 'Timestamp when record was last updated';

-- ============================================================================
-- VALIDATION NOTES
-- ============================================================================
-- 1. Code is auto-uppercased in the service layer before saving
-- 2. Name and code are unique per tenant+state+country (not globally unique)
-- 3. Service layer validates that state belongs to the specified country
-- 4. Latitude range: -90 (South Pole) to +90 (North Pole)
-- 5. Longitude range: -180 to +180 (wraps around the globe)
-- 6. Foreign keys prevent orphaned records (RESTRICT on delete)
-- 7. Case-insensitive duplicate checking handled by functional indexes
-- ============================================================================

-- Trigger for automatic updated_at timestamp
CREATE OR REPLACE FUNCTION update_cities_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_cities_updated_at
    BEFORE UPDATE ON cities
    FOR EACH ROW
    EXECUTE FUNCTION update_cities_updated_at();

-- ============================================================================
-- SAMPLE DATA (Optional - for testing)
-- ============================================================================

-- Example: Mumbai, Maharashtra, India
-- INSERT INTO cities (tenant_id, country_id, state_id, name, code, pincode, latitude, longitude, description, is_active, created_by)
-- VALUES ('TENANT001', 1, 1, 'Mumbai', 'MUM', '400001', 19.0760, 72.8777, 'Financial capital of India', true, 'SYSTEM');

-- Example: Bangalore, Karnataka, India
-- INSERT INTO cities (tenant_id, country_id, state_id, name, code, pincode, latitude, longitude, description, is_active, created_by)
-- VALUES ('TENANT001', 1, 2, 'Bangalore', 'BLR', '560001', 12.9716, 77.5946, 'IT capital of India', true, 'SYSTEM');

-- ============================================================================
-- END OF MIGRATION SCRIPT
-- ============================================================================
