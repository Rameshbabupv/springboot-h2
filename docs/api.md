# GraphQL API

## Endpoints

**GraphQL Operations**: `POST /graphql`
**GraphQL IDE**: `GET /graphiql`
**Swagger UI**: `GET /swagger-ui.html`
**Health Check**: `GET /actuator/health`

## API Versioning

No versioning in endpoint. Schema evolution via:
- Addition of new fields (backward compatible)
- Deprecation of old fields (marked `@Deprecated` in schema)
- Clients using old fields still work

## Schema Files

| File | Entities | Purpose |
|------|----------|---------|
| schema.graphqls | 12 master data | Company, Dept, Location, etc. |
| employee.graphqls | Employee CRUD | 78-field entity with filtering |
| user-management.graphqls | User, Privileges | Auth, access control |
| user-privileges.graphqls | Privilege details | Scope-based access matrix |

Total: 14 resolvers, 100% logging coverage target

## Resolvers (14 Total)

### Master Data Resolvers (12)

Each resolver provides standard CRUD + filters:

```
CompanyResolver
  ├── Query: companies (all), company (by id), searchCompanies
  ├── Mutation: createCompany, updateCompany, deleteCompany
  └── Logging: All responses via LoggingUtil

DepartmentResolver, DesignationResolver, DivisionResolver, SectionResolver
GradeResolver, JobFunctionResolver, EmploymentTypeResolver
CountryResolver, StateResolver, CityResolver
→ Same pattern (CRUD + filters)
```

### Employee Resolver

**Queries**:
- `employees(filter: EmployeeFilterCriteria)` - paginated, with scope filters
- `employee(id: Long!)` - single employee
- `searchEmployees(keyword: String)` - by name, empId, email
- `employeesByDepartment(deptId: Long)` - department lookup
- `employeesByDesignation(designationId: Long)` - designation lookup

**Mutations**:
- `createEmployee(input: EmployeeInput!)` - new employee
- `updateEmployee(id: Long!, input: EmployeeInput!)` - modify
- `deleteEmployee(id: Long!)` - remove

**Filters** (EmployeeFilterCriteria):
- company_id, location_id, division_id, department_id, section_id
- designation_id, grade_id, jobFunction_id, employmentType_id
- employmentStatus, isActive
- pagination: page (0-indexed), size (default 20)

**Response**: EmployeePageResponse with pagination metadata

### User Resolver

**Queries**:
- `users` - list all users
- `user(id: UUID!)` - single user
- `currentUser` - logged-in user details

**Mutations**:
- `createUser(input: UserAccountInput!)` - new user
- `updateUser(id: UUID!, input: UserAccountUpdateInput!)` - modify
- `changePassword(input: ChangePasswordInput!)` - password update
- `loginUser(input: LoginInput!)` - authentication

**Response**: AuthResponse with JWT token + user details

### User Privilege Resolver

**Queries**:
- `userPrivileges(userId: UUID!)` - user's access matrix
- `privilege(id: UUID!)` - privilege details

**Mutations**:
- `assignPrivilege(userId: UUID!, privilegeId: UUID!)` - grant access
- `revokePrivilege(userId: UUID!, privilegeId: UUID!)` - remove access

## Authentication

### Login Flow

```
REST POST /auth/login {username, password}
  ↓ Validate credentials (BCrypt comparison)
  ↓ Generate JWT token (8-hour expiry)
  ↓ Response: {token, refreshToken, user}

Subsequent Requests: Authorization header with JWT
  ↓ Spring Security validates token
  ↓ Extract user info from claims
  ↓ Apply organizational scope filters
```

### JWT Structure

**Header**: `Authorization: Bearer {token}`

**Payload**:
- sub: userId
- username: username
- tenantId: tenantId
- role: role
- iat: issuedAt
- exp: expiration (now + 8 hours)

**Signing**: HS256 with shared secret

### Refresh Token

- 7-day validity
- Used to obtain new access token
- POST /auth/refresh with refresh token
- Returns new access + refresh tokens

### Token Validation

Happens automatically on each GraphQL request:
- Check signature validity
- Verify expiration
- Extract user context
- Throw UnauthorizedException if invalid

## Filtering & Pagination

### Organizational Scope Filtering

User can filter on 9 dimensions. Backend enforces intersection:

```
User scope = {company: [1, 2], dept: [10, 20]}
Request = {company: 1, dept: 15}
Result = EMPTY (dept 15 not in user's authorized scope)

Request = {company: 1, dept: 10}
Result = Employees in company 1, dept 10 (authorized)
```

**Integration**: OrganizationalScopeService applies filters in service layer before querying database.

### Pagination

Standard offset/limit pattern:

**Request**:
```
{
  page: 0,      // 0-indexed
  size: 20,     // items per page
  filters: {...}
}
```

**Response**:
```
{
  content: [...],
  totalElements: 150,
  totalPages: 8,
  currentPage: 0,
  size: 20,
  hasNext: true,
  hasPrevious: false
}
```

## Error Handling

### HTTP Status Codes

| Code | Scenario |
|------|----------|
| 200 | Query/Mutation success |
| 400 | Invalid query/input validation failure |
| 401 | Missing/invalid JWT token |
| 403 | User not authorized (scope violation) |
| 404 | Resource not found |
| 500 | Server error |

### Error Response Format

```
{
  "errors": [{
    "message": "Unauthorized access to department 999",
    "extensions": {
      "classification": "UNAUTHENTICATED",
      "code": "SCOPE_VIOLATION"
    }
  }]
}
```

**Key Exceptions**:
- `UnauthorizedException` - scope violation or invalid credentials
- `ResourceNotFoundException` - entity not found
- `ValidationException` - input validation failure
- `DuplicateResourceException` - unique constraint violation

## Logging

All GraphQL responses logged with:
- **Request**: Method, parameters, timestamp
- **Response**: Result set size, execution time, user context
- **Utility**: LoggingUtil for consistent formatting

**Format** (CompanyResolver example):
```
DEBUG GraphQL Query: companies
INFO GraphQL Response: companies
Total: 15 companies
[Company(id=1, code=CORP, name=...), Company(...), ...]
```

Response logging visible in `logs/application.log`.

## Integration Points

### Frontend (React + Apollo Client)

- Apollo links configuration for automatic JWT injection
- Error handling for 401/403 responses (redirect to login)
- Cache invalidation on mutations
- Pagination handled by Apollo client

### Rate Limiting

Not implemented. Consider adding for production:
- Spring Cloud CircuitBreaker for resilience
- Token bucket algorithm for rate limiting

### CORS Configuration

Current: `http://localhost:3000`, `http://localhost:5173`

Production should whitelist specific frontend domain only.

## Query Examples

### List Employees with Filters
```
{
  employees(filter: {
    page: 0
    size: 20
    companyId: 1
    departmentId: 10
    employmentStatus: "ACTIVE"
  }) {
    content { id, empId, employeeName, designation, department }
    totalElements, totalPages, hasNext
  }
}
```

### Search Employee
```
{
  searchEmployees(keyword: "john") {
    id, empId, employeeName, email, department
  }
}
```

### Nested Query (Employee with Relations)
```
{
  employee(id: 1) {
    id, empId, employeeName
    company { code, name }
    department { code, name }
    designation { code, name }
  }
}
```

---
**Status**: 14 resolvers operational. Logging implementation in progress.
