# Architecture

## System Overview

Multi-tenant SaaS HRMS built on Spring Boot 3.2.0 with GraphQL-first API design. PostgreSQL backend with organizational scope-based security.

**Tech Stack**: Java 17 | Spring Boot 3.2.0 | GraphQL | PostgreSQL | Spring Data JPA | JWT | BCrypt

## Layered Architecture

```
Presentation Layer
    ↓ (GraphQL/REST)
API Layer (Resolvers)
    ↓
Business Logic (Services)
    ↓
Data Access (Repositories)
    ↓
Database (PostgreSQL)
```

## Component Integration

### API Layer
- **Primary**: GraphQL endpoint (`/graphql`) - all business data operations
- **Secondary**: REST endpoints - authentication only (`/auth/login`, `/auth/refresh`)
- **IDE**: GraphiQL available at `/graphiql`
- **14 Resolvers**: Company, Employee, User, Location, Department, Designation, Division, Section, Grade, JobFunction, EmploymentType, Country, State, City

### Service Layer
- 24 service implementations handling business logic
- Pattern: Interface + Implementation (`*Service.java` + `*ServiceImpl.java`)
- OrganizationalScopeService enforces data boundary security
- All CRUD operations + filtering logic

### Data Access Layer
- 28 JPA repositories (Spring Data)
- Composite queries for organizational filtering
- 19 indexes on Employee table for query optimization

### Security Architecture

**Authentication**: JWT (8-hour expiry, 7-day refresh)
- Issued on login via REST endpoint
- Validated on each GraphQL request
- Token stored in Authorization header

**Authorization**: Role-based Organizational Scope
- 3-tier role hierarchy: Super Admin > Admin > User
- 9-dimensional organizational filters: Company, Location, Division, Department, Section, Designation, Grade, JobFunction, EmploymentType
- User's accessible boundaries stored in `user_organizational_scope` table
- Backend enforces intersection of (requested_filters ∩ user_authorized_scope)
- Throws UnauthorizedException on scope violation

**Multi-Tenancy**: Tenant isolation via `tenantId`
- Every entity includes `tenantId` field
- Queries implicitly filtered by user's tenantId
- Database constraint prevents cross-tenant data access

## Data Flow

### Read Operation
```
Frontend → GraphQL Query → Resolver → OrganizationalScopeService
→ Apply Security (intersection logic) → ServiceImpl → Repository Query
→ PostgreSQL → Response logged via LoggingUtil → Frontend
```

### Write Operation
```
Frontend → GraphQL Mutation → Resolver (validate scope)
→ ServiceImpl → Repository → PostgreSQL → Log response → Frontend
```

### User Access Control Flow
```
User makes request → JWT validated → Extract tenantId, userId
→ Fetch user's scope boundaries → Apply to all queries
→ Only authorized data returned
```

## Integration Points

### Frontend (React + Apollo Client)
- Consumes GraphQL `/graphql` endpoint
- Handles JWT tokens (login required)
- Apollo Client cache for optimizations
- CORS configured for localhost:3000 and localhost:5173

### Database Interactions
- Hibernate auto-updates schema (ddl-auto=update)
- Connection pooling: HikariCP default
- Indexes on high-query-volume tables (especially Employee with 78 fields)
- Foreign key relationships enforced at DB level

### Logging Pipeline
- All GraphQL responses logged via LoggingUtil
- Request/response pattern: DEBUG for entry, INFO for output
- SLF4J + Logback (NOT Log4j)
- Logs written to `logs/application.log` with rotation

## Configuration

**Port**: 8090
**Database**: PostgreSQL localhost:5432, database=hrmsdb
**GraphQL Endpoint**: `http://localhost:8090/graphql`
**GraphQL IDE**: `http://localhost:8090/graphiql`
**Swagger**: `http://localhost:8090/swagger-ui.html`

**JWT Configuration**:
- Access Token: 28800 seconds (8 hours)
- Refresh Token: 7 days
- Algorithm: HS256

**Password Policy**:
- Expiry: 90 days
- Failed login threshold: 5 attempts
- Account lock duration: 30 minutes
- Max concurrent sessions: 3

## Deployment Considerations

- Single Spring Boot JAR: `hrms-saas-1.0.0.jar`
- Stateless design (JWT-based, no session storage)
- Database migration: Hibernate DDL auto handles schema
- CORS whitelist should be tightened for production (remove localhost:3000)
- JWT secret should be externalized to environment variables
- Password hashing via BCrypt (10 rounds)

## Master Data Entities (12 Total)

All support full CRUD via GraphQL:
Company → CompanyLocation → Department → Designation → Division → Section → Grade → JobFunction → EmploymentType → Country → State → City

Organizational hierarchy: Company → Location → Division → Department → Section

## Extensions & Future

- Attendance module (planned, docs in `/docs/yyyy-mmm-dd/`)
- Testing framework (decision pending on strategy - see `/docs/tasks/`)
- CI/CD pipeline (not yet implemented)
- Performance monitoring (JMeter load testing pending)

---
**Status**: Core architecture complete and running. Testing infrastructure to be designed.
