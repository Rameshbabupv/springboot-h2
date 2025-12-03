# Development Setup

## Prerequisites

- Java 17 (JDK, not JRE)
- Maven 3.9.6+
- PostgreSQL 12+
- Git

Verify installations:
```bash
java -version
mvn -v
psql --version
git --version
```

## Database Setup

### PostgreSQL Configuration

Create database and user:
```sql
CREATE DATABASE hrmsdb;
CREATE USER postgres WITH PASSWORD 'Admin@123';
ALTER ROLE postgres WITH SUPERUSER;
```

**Connection Details**:
- Host: localhost
- Port: 5432
- Database: hrmsdb
- User: postgres
- Password: Admin@123

Application properties are pre-configured in `src/main/resources/application.properties`:
- URL: `jdbc:postgresql://localhost:5432/hrmsdb`
- Hibernate DDL: `update` (auto-creates/updates schema on startup)

### Data Initialization

On first run, application initializes master data via `DataInitializer.java`:
- All 12 master entities populated with seed data
- Employee template structure created
- Sample organizations, departments, designations setup

No manual SQL scripts needed for local development.

## Project Structure

```
HRMS_New_Api/
├── src/main/java/com/hrms/
│   ├── HrmsApplication.java (entry point)
│   ├── config/ (CORS, Swagger, Security, GraphQL)
│   ├── controller/ (REST - auth only)
│   ├── dto/ (request/response data transfer objects)
│   ├── entity/ (JPA entities, 15+ types)
│   ├── exception/ (custom exceptions)
│   ├── graphql/ (resolvers + inputs)
│   ├── mapper/ (MapStruct DTO-Entity mapping)
│   ├── repository/ (28 Spring Data JPA repositories)
│   ├── resolver/ (user privileges resolver)
│   ├── service/ (interfaces)
│   ├── service/impl/ (24 implementations)
│   └── util/ (LoggingUtil, JWT, Password utils)
├── src/main/resources/
│   ├── application.properties (database, port, logging config)
│   └── graphql/ (4 schema files)
├── pom.xml (Maven dependencies)
├── target/ (compiled output, JAR file)
└── docs/ (this directory)
```

## Build & Run

### Clean Build
```bash
mvn clean compile
```

Compiles 240 Java source files. No errors expected.

### Package Application
```bash
mvn clean package
```

Produces `target/hrms-saas-1.0.0.jar` (~50MB)

### Run Application
Option 1 - Maven:
```bash
mvn spring-boot:run
```

Option 2 - JAR:
```bash
java -jar target/hrms-saas-1.0.0.jar
```

**Expected startup**:
- Logs show PostgreSQL connection successful
- Hibernate creates/updates schema
- DataInitializer populates master data
- Application ready on http://localhost:8090

### Verify It's Running

```bash
# GraphQL query test
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ companies { id code name } }"}'

# Or open GraphQL IDE
http://localhost:8090/graphiql
```

## Development Workflow

### Git Workflow (Git Flow)

**Branches**:
- `main` - production-ready code
- `develop` - integration branch for features

**Creating feature**:
```bash
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name
# Make changes
git add .
git commit -m "Add your feature"
git push origin feature/your-feature-name
# Create Pull Request via GitHub
```

**Merging**:
- All changes via Pull Requests (no direct pushes to develop/main)
- Merge strategy: Merge commit (--no-ff)
- Preserves branch history and context

### IDE Configuration

**IntelliJ IDEA**:
- Open project root
- Maven automatically detected
- Project Structure → Project SDK → Java 17
- Enable annotation processing for Lombok + MapStruct

**VS Code**:
- Install Java Extension Pack
- Install REST Client extension (optional, for testing endpoints)
- pom.xml automatically recognized

### Dependency Management

- Spring Boot 3.2.0 (managed by parent POM)
- Lombok 1.18.30 (reduces boilerplate)
- MapStruct 1.5.5 (DTO-Entity mapping)
- JJWT 0.12.3 (JWT handling)
- Spring Data JPA (ORM)
- PostgreSQL JDBC driver

Update dependencies:
```bash
mvn dependency:update-snapshots
```

## Common Issues

**PostgreSQL Connection Refused**
- Verify PostgreSQL running: `psql -U postgres -d postgres`
- Check credentials in `application.properties`
- Ensure database `hrmsdb` exists

**Port 8090 Already in Use**
- Change port in `application.properties`: `server.port=8091`
- Or kill process: `lsof -i :8090` then `kill -9 <PID>`

**Hibernate Schema Conflicts**
- Delete table and let Hibernate recreate: `DROP TABLE IF EXISTS table_name CASCADE;`
- Or truncate: `TRUNCATE TABLE table_name RESTART IDENTITY;`

**Maven Build Slow**
- Check internet connection (downloading dependencies first time)
- Increase Maven heap: `export MAVEN_OPTS="-Xmx1024m"`

**Lombok Not Working**
- Ensure annotation processing enabled in IDE
- IntelliJ: File → Settings → Build → Annotation Processors → Enable
- Rebuild project

## Logging

**Configuration**: `src/main/resources/application.properties`

**Levels**:
- DEBUG: Method entry, request details
- INFO: GraphQL responses (via LoggingUtil)
- WARN: Potential issues
- ERROR: Exception details

**Output**: `logs/application.log` with rotation

**View logs**:
```bash
tail -f logs/application.log  # Real-time
grep "GraphQL" logs/application.log  # Filter
```

## Testing Locally

**GraphQL Queries** via GraphiQL:
1. Open http://localhost:8090/graphiql
2. Login mutation to get JWT token
3. Add Authorization header with token
4. Execute queries

**REST Authentication**:
```bash
POST /auth/login
{
  "username": "admin",
  "password": "password"
}
```

**Filtering** (Organizational Scope):
```bash
POST /graphql
Query: employees with scope filters
Expected: Only authorized data returned
```

---
**Status**: All setup complete. Ready for development.
