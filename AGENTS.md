# Repository Guidelines

## Project Structure & Module Organization
Source lives under `src/main/java/com/hrms`, split into `controller`, `service`, `entity`, and GraphQL `resolver`/`input` packages so keep new components in their respective layer (e.g., place a payroll resolver beside `graphql/resolver/EmployeeResolver.java`). Shared payloads live in `dto/**/*`, and GraphQL schemas plus Spring config stay in `src/main/resources` (`graphql/schema.graphqls` and `application.properties`). Documentation relevant to design, deployment, and troubleshooting is collected in `docs/`, while SQL helpers reside in `sql/` and ready-to-run migration templates in `scripts/`. Build artifacts land in `target/` and logs stream to `logs/hrms-application.log`.

## Build, Test, and Development Commands
- `mvn clean package` – compile with Java 17, run unit tests, and produce `target/hrms-saas-1.0.0.jar`.
- `mvn spring-boot:run` – boot the API directly from sources with hot reload friendly feedback.
- `java -jar target/hrms-saas-1.0.0.jar` – run the packaged jar in a prod-like mode.
- `mvn test` – execute the Spring/JPA/GraphQL tests; add `-Dspring.profiles.active=test` for isolated config.

## Coding Style & Naming Conventions
Follow standard Spring Boot conventions: 4-space indentation, `{` on the same line, and descriptive class names (`CompanyController`, `EmployeeServiceImpl`). Package names stay lowercase. GraphQL inputs/resolvers mirror entity names (`DepartmentInput`, `DepartmentResolver`), and REST endpoints use plural nouns (`/api/employees`). Rely on Lombok annotations (`@Getter`, `@RequiredArgsConstructor`) and MapStruct mappers to keep DTO conversion centralized. Validate inbound payloads with `@Valid` and constraint annotations before delegating to services.

## Testing Guidelines
JUnit 5, Spring Boot Test, and `spring-graphql-test` are already on the classpath. Place new tests under `src/test/java/com/hrms/...` with filenames ending in `Tests` (e.g., `CompanyControllerTests`). Favor the embedded H2 profile for repository/service coverage and use GraphQL tester slices for resolver flows. Target at least 80% line coverage for new modules and assert both REST `ApiResponse` envelopes and GraphQL schema contracts. Run `mvn test` locally before opening a PR.

## Commit & Pull Request Guidelines
Adhere to the documented Git Flow: create feature branches from `develop` (`feature/<jira-key>-short-title`), release branches from `develop`, and hotfix branches from `main`. Commits use the imperative mood and reference tracking IDs when applicable ("Add payroll accrual resolver", "JIRA-456: Harden auth"). Every PR should summarize scope, list test evidence (`mvn test`, manual GraphQL call), link the issue, and include screenshots when the change affects REST/GraphQL payloads. Merge via the "Create merge commit" option only after at least one approval and passing status checks.

## Security & Configuration Tips
`src/main/resources/application.properties` contains placeholder PostgreSQL and JWT settings—replace them with environment variables or externalized config before deploying. Keep secrets out of commits, rotate the `jwt.secret`, and review logging overrides so sensitive tenant data is not emitted at DEBUG. When running locally, confirm H2-only data is isolated, and document any schema migrations in `docs/database.md` plus the matching file under `sql/`.
