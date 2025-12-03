# Security Architecture

## Authentication

### JWT-Based (Token-Stateless)

**Flow**:
1. User submits credentials (username + password) via REST POST /auth/login
2. Backend validates against user table (BCrypt password comparison)
3. Issues JWT token (8-hour validity) + refresh token (7-day validity)
4. Client includes Authorization header on subsequent requests
5. Spring Security filter validates token on each GraphQL request

**Token Details**:
- Algorithm: HS256 (symmetric key signing)
- Expiry: 28800 seconds (8 hours) - configurable in application.properties
- Payload: userId, username, tenantId, role, timestamp
- Signing Key: Application secret (should be externalized in production)

**Refresh Token**:
- 7-day validity
- Used to obtain new access token without re-authenticating
- Endpoint: POST /auth/refresh
- Response: New access token + new refresh token

### Password Security

**Hashing**: BCrypt with 10 rounds
- Never stored plaintext
- Comparison: On login, hash submitted password and compare stored hash
- Cost: ~100ms per hash (intentional slowdown for brute-force resistance)

**Password Policy** (configurable):
- Expiry: 90 days from set/last change
- Minimum complexity: 8 chars (configurable)
- Failed login threshold: 5 attempts → account lock
- Lock duration: 30 minutes (automatic unlock)
- History: Last 3 passwords not allowed for reuse

**Password Reset**:
- User initiates reset via forgot-password endpoint
- System generates random token (UUID)
- Token sent via email (in production)
- Token valid for 24 hours
- User sets new password with token
- Token invalidated after use

## Authorization

### Role-Based Access Control (3-Tier)

**Hierarchy**:
```
Super Admin (full access, no restrictions)
    ↓ (overrides below)
Admin (can manage users/data within their scope)
    ↓ (overrides below)
User (can view/edit assigned data only)
```

**Roles stored** in users.role column (enum: SUPER_ADMIN, ADMIN, USER)

**Assignment**: Assigned by Super Admin or Admin
- Can be changed via updateUser mutation
- Role changes take effect on next token refresh

### Privilege-Based Access (9-Dimensional)

**System**: Orthogonal to roles. Defines **what data** user can access.

**9 Dimensions**:
```
Company       (which companies)
Location      (which branches)
Division      (which divisions)
Department    (which departments)
Section       (which sections)
Designation   (which job titles)
Grade         (which salary grades)
JobFunction   (which job categories)
EmploymentType (Full-time, Contract, etc.)
```

**Storage**: `user_organizational_scope` table
- One record per user per scope dimension
- Each dimension can be multi-valued: {company: [1, 2], dept: [10, 20]}
- Super Admin: All dimensions = ALL (no restrictions)

**Enforcement**: Backend intersection logic
```
User authorized scope: {company: [1, 2], dept: [10, 20]}
Query filters: {company: 1, dept: 15}
Result: UNAUTHORIZED (dept 15 not in [10, 20])

Query filters: {company: 1, dept: 10}
Result: OK (both in authorized scope)
```

## Multi-Tenancy

### Isolation Strategy

**Tenant ID**: String field on every entity (company identifier)
- Assigned on user creation
- Used to filter all queries implicitly
- Prevents cross-tenant data access

**Enforcement Points**:
1. **Service Layer**: OrganizationalScopeService filters by tenantId
2. **GraphQL Resolver**: Extracts tenantId from JWT claims
3. **Repository**: Queries include tenantId in WHERE clause
4. **Database**: Constraints prevent orphaned records

**Example Query Enforcement**:
```
SELECT * FROM employees
WHERE tenant_id = '{user_tenant_id}'
  AND id = {requested_id}
  AND (dept_id IN user_authorized_depts)
```

### Data Isolation Guarantee

- No application code can query across tenants
- Even if developer mistakenly omits tenantId filter, Spring Security injects it
- TenantId extracted from JWT token (user context)
- Cryptographic enforcement: changing tenantId in JWT requires signing key

## Organizational Scope Security

### Components

1. **UserOrganizationalScope Entity**
   - Stores user's authorized boundaries
   - 9 columns: company_id, location_id, dept_id, etc.
   - Null = no restriction on that dimension (Super Admin use case)

2. **OrganizationalScopeService**
   - Loaded on each request
   - Applies intersection filters before database query
   - Throws UnauthorizedException on violation

3. **Backend Enforcement**
   - Cannot be bypassed from frontend
   - Runs on every query
   - Stateless (extracted from database each request)

### Application Flow

```
GraphQL Request (user=john, filters={dept:15})
  ↓ Spring Security extracts tenantId, userId from JWT
  ↓ Resolver calls: OrganizationalScopeService.applySecurityScope(userId, filters)
  ↓ Service loads: SELECT * FROM user_organizational_scope WHERE user_id = john
  ↓ Service checks: Is dept 15 in john's authorized departments?
  ↓ If YES: Apply filters, query database, return data
  ↓ If NO: Throw UnauthorizedException
```

### Example Scenario

**User Context**:
- John (userId=1) is HR Manager for Bangalore location only
- Authorized scope: {location_id: [bangalore], department_id: [10, 20]}

**Query 1** - Allowed:
```
{
  employees(filter: {location: bangalore, department: 10})
}
→ ALLOWED: Both location and dept in John's scope
```

**Query 2** - Blocked:
```
{
  employees(filter: {location: mumbai})
}
→ UNAUTHORIZED: Mumbai not in John's authorized locations
```

## Session Management

### Concurrent Session Limit

**Policy**: Max 3 concurrent sessions per user
- Enforced at application level (configurable)
- 4th login invalidates oldest session
- Prevents credential sharing

**Implementation**: UserSessionService tracks active sessions

### Session Data

- Session token (separate from JWT)
- Created timestamp
- Expiration timestamp (matches JWT expiry)
- Device/IP info (optional, for audit trail)

### Logout

```
POST /auth/logout (with Authorization header)
  ↓ Backend marks session as expired
  ↓ Token can no longer be refreshed
  ↓ Frontend removes token from local storage
```

## Audit & Logging

### User Activity Logging

**UserActivityLog Table** tracks:
- User ID
- Action (login, logout, query, mutation, failed_login)
- Resource accessed
- Timestamp
- Result (success/failure)
- IP address (optional)

**Query Example**:
```
SELECT * FROM user_activity_log
WHERE user_id = 'john'
  AND action IN ('login', 'failed_login')
  AND created_at > now() - interval '7 days'
```

### Sensitive Operations Audit

- Password changes
- Privilege assignments
- User creation/deletion
- Data exports
- Organizational scope modifications

All logged with user context and timestamp.

## Security Headers

**Recommended** (implement in production):
- `Strict-Transport-Security`: HTTPS only
- `X-Content-Type-Options`: nosniff
- `X-Frame-Options`: DENY (prevent clickjacking)
- `Content-Security-Policy`: Restrict scripts/resources
- `X-XSS-Protection`: 1; mode=block

## Input Validation

### GraphQL Input Validation

- Email format validation (RFC 5322 regex)
- Phone number format validation
- Aadhar/PAN format validation (Indian compliance)
- String length limits (prevent buffer overflow)
- Enum validation (only valid values accepted)

### Sanitization

- HTML special characters escaped in string fields
- SQL injection prevented by parameterized queries (JPA)
- XSS prevention by never trusting user input in display

## CORS Configuration

**Current** (Development):
```
Allowed origins: localhost:3000, localhost:5173
Allowed methods: GET, POST, OPTIONS
Allowed headers: Content-Type, Authorization
Credentials: true (cookies allowed)
```

**Production Changes Needed**:
- Remove localhost entries
- Add production frontend domain only
- Disable credentials if not needed
- Add max-age for preflight caching

## SSL/TLS

**Development**: HTTP only (acceptable for localhost)

**Production Mandatory**:
- Force HTTPS (HTTP → HTTPS redirect)
- Valid SSL certificate
- TLS 1.2 minimum (ideally 1.3)
- Certificate pinning (optional, for API clients)

## Secrets Management

**Current** (Development):
- Hardcoded in application.properties (NOT production-safe)

**Production Required**:
- Environment variables for sensitive values
- Vault service (HashiCorp, AWS Secrets Manager)
- JWT signing key externalized
- Database credentials externalized
- Never commit secrets to git

**Example**:
```
export DB_PASSWORD=<secret>
export JWT_SECRET=<secret>
java -jar app.jar
```

## Compliance

### GDPR

- User data deletion capability (right to be forgotten)
- Data export functionality
- Consent tracking (if applicable)
- Audit logs for access

### Indian Compliance

- Aadhar masking (last 4 digits visible only)
- PAN format validation
- GST compliance in Company entity
- Data residency (PostgreSQL in India region)

---
**Status**: Authentication + authorization complete. Rate limiting + advanced audit pending.
