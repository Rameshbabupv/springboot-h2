-- ============================================
-- Company Setup Module - Complete Schema
-- Execute all table creation scripts in order
-- ============================================

-- Set client encoding
SET client_encoding = 'UTF8';

-- Display execution info
\echo '================================================'
\echo 'Company Setup Module - Database Schema Creation'
\echo '================================================'
\echo ''

-- Table 1: Company (Main table)
\echo 'Creating Table 1: company...'
\i 01_company_table.sql
\echo 'Table 1: company - DONE'
\echo ''

-- Table 2: Company Statutory
\echo 'Creating Table 2: company_statutory...'
\i 02_company_statutory_table.sql
\echo 'Table 2: company_statutory - DONE'
\echo ''

-- Table 3: Company General Settings
\echo 'Creating Table 3: company_general_settings...'
\i 03_company_general_settings_table.sql
\echo 'Table 3: company_general_settings - DONE'
\echo ''

-- Table 4: Company Locations
\echo 'Creating Table 4: company_location...'
\i 04_company_location_table.sql
\echo 'Table 4: company_location - DONE'
\echo ''

-- Table 5: Company Bank Accounts
\echo 'Creating Table 5: company_bank_account...'
\i 05_company_bank_account_table.sql
\echo 'Table 5: company_bank_account - DONE'
\echo ''

\echo '================================================'
\echo 'All Company Setup tables created successfully!'
\echo '================================================'
\echo ''
\echo 'Summary:'
\echo '  1. company                     - Main company table'
\echo '  2. company_statutory           - Tax IDs and compliance'
\echo '  3. company_general_settings    - Org structure and system config'
\echo '  4. company_location            - Multiple locations'
\echo '  5. company_bank_account        - Bank accounts'
\echo ''
