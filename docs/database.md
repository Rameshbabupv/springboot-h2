# Database Schema

## Connection

**Type**: PostgreSQL 12+
**Host**: localhost
**Port**: 5432
**Database**: hrmsdb
**User**: postgres

Hibernate auto-manages schema creation/updates via `ddl-auto=update`.

## Core Tables (16)

### Master Data (12 entities)

| Table | Purpose | Key Fields |
|-------|---------|-----------|
| companies | Organization master | id, tenantId, code, name, gst, pan, tan, cin |
| company_locations | Branch locations | id, tenantId, company_id, location_name, address |
| departments | Organizational units | id, tenantId, code, name, company_id |
| designations | Job positions | id, tenantId, code, name, level |
| divisions | Company divisions | id, tenantId, code, name, company_id |
| sections | Department subdivisions | id, tenantId, code, name, department_id |
| grades | Salary grades | id, tenantId, code, name, min_salary, max_salary |
| job_functions | Job categories | id, tenantId, code, name, description |
| employment_types | Employment classifications | id, tenantId, code, name (Full-time, Contract, etc.) |
| countries | Geographic | id, tenantId, code, name, currency, phone_code |
| states | Geographic | id, tenantId, code, name, country_id |
| cities | Geographic | id, tenantId, code, name, state_id |

### Core Business Entities

| Table | Purpose | Fields |
|-------|---------|--------|
| employees | Employee records | 78 columns, 13 required |
| users | User accounts | username, email, password_hash, role |
| user_privileges | Access control | user_id, privilege_id, scope |
| user_organizational_scope | Data boundary | user_id, company_id, location_id, dept_id, etc. (9 dimensions) |

## Employee Table Details

**78 columns total**:

**13 Required Fields** (Indian statutory minimum):
- tenantId, company, location, department, designation, jobFunction, employmentType
- empId, employeeName, dateOfJoin
- dateOfBirth, gender
- aadharNo, panNo

**65 Optional Fields** (categorized):
- Contact: email, phone, mobile, address
- Personal: maritalStatus, bloodGroup, nationality
- Bank: bankName, accountNumber, ifscCode, branchName
- Insurance: lifeInsuranceNo, healthInsuranceNo
- Compensation: salary, bonus, allowances
- Dates: endOfContract, probationEndDate, confirmationDate
- Status: employmentStatus, isActive, department transfer history
- Reporting: reportingManagerId, dotReportingManagerId
- Plus 40+ additional fields for compliance

**Indexes** (19 total):
- 7 new composite indexes for filtering optimization
- 12 existing single-column indexes

**Critical indexes for filtering**:
- (tenantId, company_id, employmentStatus)
- (tenantId, department_id, designation_id)
- (tenantId, location_id, isActive)
- Single column: email, empId, name, dateOfJoin

## Relationships

### Organizational Hierarchy
```
Company
  ├── CompanyLocation (1:N)
  │   └── Employees (1:N)
  └── Division (1:N)
      └── Department (1:N)
          ├── Section (1:N)
          │   └── Employees (1:N)
          └── Employees (1:N)
              ├── Designation (N:1)
              ├── Grade (N:1)
              ├── JobFunction (N:1)
              └── EmploymentType (N:1)

Geographic Hierarchy
Country (1:N) → State (1:N) → City (1:N)
```

### User Access Control
```
User (1:N) → UserPrivilege (N:M) → Privilege
User (1:N) → UserOrganizationalScope (describes boundaries)
```

**user_organizational_scope** stores what data user can access across 9 dimensions:
- company_id, location_id, division_id, department_id, section_id
- designation_id, grade_id, job_function_id, employment_type_id

When user queries employees, backend enforces:
```
result = Employee.filter(requested_filters ∩ user_scope)
```

## Data Consistency

### Foreign Keys
- Enforced at database level
- DELETE: CASCADE for subordinate entities
- UPDATE: CASCADE for reference updates
- Prevents orphaned records

### Unique Constraints
- (tenantId, company_code) - company codes unique per tenant
- (tenantId, employee_id) - employee IDs unique per tenant
- (tenantId, user_username) - usernames unique per tenant
- (email) - globally unique (for auth)

### Multi-Tenancy Enforcement

Every record includes `tenantId` (String):
- Queries filtered by user's tenantId
- No cross-tenant queries possible
- Database doesn't enforce FK across tenants (by design - multi-tenant isolation)

Example query enforcement:
```
SELECT * FROM employees
WHERE tenant_id = '{user_tenant}' AND id = {requested_id}
```

## Audit Columns

All entities include:
- `created_at` (LocalDateTime, auto-set on insert)
- `updated_at` (LocalDateTime, auto-updated)
- `created_by` (String, username)
- `updated_by` (String, username)

Managed via `@CreationTimestamp`, `@UpdateTimestamp` JPA annotations.

## Performance Considerations

### Index Strategy
- Composite indexes on frequently filtered columns
- Employee table gets special attention (78 fields, high query volume)
- Individual indexes on foreign key columns
- Index on `(tenantId, isActive)` for status filtering

### Query Optimization
- Lazy loading on collections (JOIN FETCH in service layer)
- Pagination enforced for large result sets
- Organizational scope filtering happens at SQL level (not application)

### Connection Pooling
- HikariCP default configuration
- Max pool size: 10 (configurable in application.properties)
- Connection timeout: 30 seconds
- Idle timeout: 10 minutes

## Migration & Maintenance

### Schema Updates
Hibernate handles via `ddl-auto=update`:
- New columns added automatically
- Type changes require manual migration
- Never use `create-drop` in production

### Data Backups
Recommended:
- Daily PostgreSQL dumps: `pg_dump hrmsdb > backup.sql`
- WAL archiving for point-in-time recovery
- Test restores regularly

### Cleanup Operations
Remove test data:
```bash
TRUNCATE TABLE employees RESTART IDENTITY CASCADE;
-- Cascades to dependent tables
```

Reset sequences:
```bash
ALTER SEQUENCE seq_name RESTART WITH 1;
```

## User & Authentication Schema

### users table
- id (UUID)
- username (unique, indexed)
- email (unique, indexed)
- password_hash (bcrypt, 60 chars)
- role (Super Admin, Admin, User)
- account_status (ACTIVE, LOCKED, DISABLED)
- last_login (LocalDateTime)
- failed_login_count (int, resets on successful login)
- account_locked_until (LocalDateTime, null if not locked)

### Password Policy Table (implicit via UserPasswordReset)
- user_id
- reset_token
- reset_token_expiry
- password_expiry_date
- new_password_hash (for pending resets)

### Sessions (user_session if tracked)
- user_id
- session_token
- created_at
- expires_at
- max_concurrent: 3 sessions per user

---
**Schema generated**: Automatically by Hibernate
**Last synchronized**: 2025-Dec-03
**Migration scripts**: `/sql/` directory for custom migrations
