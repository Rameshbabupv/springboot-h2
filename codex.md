# HRMS_New_Api — Working Notes

Quick reference for future sessions so we do not have to rescan the entire tree.

## App Overview
- Spring Boot 3.2 / Java 17 monolith located in `src/main/java/com/hrms`.
- Primary API is GraphQL (resolvers under `graphql/resolver`), REST controllers mainly for legacy modules and auth.
- Database: PostgreSQL (`application.properties` points to `jdbc:postgresql://localhost:5432/hrmsdb`). Hibernate ddl-auto=update.
- Authentication intended via JWT (utility in `util/JwtUtil.java`), but current `SecurityConfig` simply permits all requests.

## Key Modules
| Area | Notes |
| --- | --- |
| Entities | Comprehensive HR domain: `Company`, `Employee`, leave entities, user/accounting tables. Multi-tenant via `tenantId` column. |
| Services | `service/*.java` define interfaces; implementations under `service/impl`. `EmployeeServiceImpl` handles MapStruct DTO mapping plus Criteria filter queries. |
| GraphQL | Schema files in `src/main/resources/graphql`. Resolvers convert GraphQL inputs to DTOs and call services. Logging via `LoggingUtil`. |
| Security | JWT utilities exist, but `SecurityConfig` is a placeholder (CSRF disabled, everything `permitAll`). Organizational scope enforcement lives in `OrganizationalScopeServiceImpl`. |
| Docs | Extensive markdown in `docs/` (architecture, setup, security, dated logs). README still mentions H2+8080 even though config is Postgres+8090. |

## Observations / TODO Reminders
1. Align security: implement JWT filter and derive tenant/user context from token instead of GraphQL arguments.
2. Externalize secrets (DB password, `jwt.secret`) — currently plain text in `application.properties`.
3. Remove leftover debug prints in inputs (e.g., `CompanyInput` setters log to stdout).
4. Refresh README to reflect current stack/port/database.

Use this file as a lightweight scratchpad; update if architecture shifts so we avoid re-discovery.
