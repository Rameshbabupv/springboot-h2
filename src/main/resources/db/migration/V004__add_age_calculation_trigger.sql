-- Migration: V004__add_age_calculation_trigger.sql
-- Date: December 17, 2025
-- Purpose: Add triggers to automatically calculate employee age from date_of_birth
-- Database: PostgreSQL

-- =====================================================
-- CREATE FUNCTION FOR AGE CALCULATION (INSERT)
-- =====================================================
CREATE OR REPLACE FUNCTION calculate_age_on_insert()
RETURNS TRIGGER AS $$
BEGIN
  -- Calculate age automatically when dateOfBirth is provided
  IF NEW.date_of_birth IS NOT NULL THEN
    NEW.age := EXTRACT(YEAR FROM AGE(NEW.date_of_birth));
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- CREATE TRIGGER FOR INSERT OPERATION
-- =====================================================
DROP TRIGGER IF EXISTS calculate_age_on_insert ON employees CASCADE;
CREATE TRIGGER calculate_age_on_insert
BEFORE INSERT ON employees
FOR EACH ROW
EXECUTE FUNCTION calculate_age_on_insert();

-- =====================================================
-- CREATE FUNCTION FOR AGE CALCULATION (UPDATE)
-- =====================================================
CREATE OR REPLACE FUNCTION calculate_age_on_update()
RETURNS TRIGGER AS $$
BEGIN
  -- Recalculate age when dateOfBirth is updated
  IF NEW.date_of_birth IS NOT NULL THEN
    NEW.age := EXTRACT(YEAR FROM AGE(NEW.date_of_birth));
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- CREATE TRIGGER FOR UPDATE OPERATION
-- =====================================================
DROP TRIGGER IF EXISTS calculate_age_on_update ON employees CASCADE;
CREATE TRIGGER calculate_age_on_update
BEFORE UPDATE ON employees
FOR EACH ROW
WHEN (OLD.date_of_birth IS DISTINCT FROM NEW.date_of_birth)
EXECUTE FUNCTION calculate_age_on_update();

-- =====================================================
-- UPDATE EXISTING RECORDS (if any have date_of_birth but no age)
-- =====================================================
UPDATE employees
SET age = EXTRACT(YEAR FROM AGE(date_of_birth))
WHERE date_of_birth IS NOT NULL AND age IS NULL;

-- =====================================================
-- VERIFICATION QUERIES (can be run after migration)
-- =====================================================
-- SELECT trigger_name FROM information_schema.triggers WHERE trigger_name LIKE 'calculate_age%';
-- SELECT id, employee_name, date_of_birth, age FROM employees WHERE date_of_birth IS NOT NULL LIMIT 5;
