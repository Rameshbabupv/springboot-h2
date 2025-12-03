# GraphQL Logging Quick Start Guide

## What Was Implemented

### 1. Enhanced Logging Configuration
**File**: `src/main/resources/application.properties`

- Comprehensive logging levels for all com.hrms packages
- File logging with automatic rotation (10MB files, 30-day retention)
- Logs saved to: `logs/hrms-application.log`

### 2. GraphQL Resolvers Updated
**Location**: `src/main/java/com/hrms/graphql/resolver/`

✅ **Fully Implemented** (4 resolvers with complete logging):
- `CompanyResolver.java` - 25 methods logged
- `EmployeeResolver.java` - 15 methods logged
- `UserAccountResolver.java` - 28 methods logged
- `DepartmentResolver.java` - 9 methods logged

⚠️ **Ready for Logging** (10 resolvers with @Slf4j added):
- `DesignationResolver.java`
- `DivisionResolver.java`
- `SectionResolver.java`
- `GradeResolver.java`
- `JobFunctionResolver.java`
- `EmploymentTypeResolver.java`
- `CountryResolver.java`
- `StateResolver.java`
- `CityResolver.java`
- `EmployeeTemplateResolver.java`

### 3. Service Layer Logging
**Location**: `src/main/java/com/hrms/service/impl/`

- All 24 service implementations have `@Slf4j` ready
- Example logging implemented in `CompanyServiceImpl.java`

---

## How to Add Logging to Remaining Files

### Template for Query Methods

```java
@QueryMapping
public List<Entity> entities(@Argument String tenantId) {
    log.debug("GraphQL Query: entities - tenantId: {}", tenantId);
    List<Entity> result = service.getEntities(tenantId);
    log.info("GraphQL Response: entities - returned {} items", result.size());
    return result;
}
```

### Template for Mutation Methods

```java
@MutationMapping
public Entity createEntity(@Argument EntityInput input) {
    log.debug("GraphQL Mutation: createEntity - name: {}", input.getName());
    EntityRequest request = mapToRequest(input);
    Entity result = service.createEntity(request);
    log.info("GraphQL Response: createEntity - created id: {}", result.getId());
    return result;
}
```

### Template for Service Methods

```java
@Override
public List<Entity> getAllEntities() {
    log.debug("Fetching all entities");
    List<Entity> result = repository.findAll();
    log.info("Service Response: getAllEntities - returned {} entities", result.size());
    return result;
}
```

---

## Quick Copy-Paste Examples

### For Remaining Resolvers (Already have @Slf4j)

Just update each method following this pattern:

**Before**:
```java
@QueryMapping
public List<Designation> designations() {
    return designationService.getAllDesignations();
}
```

**After**:
```java
@QueryMapping
public List<Designation> designations() {
    log.debug("GraphQL Query: designations");
    List<Designation> result = designationService.getAllDesignations();
    log.info("GraphQL Response: designations - returned {} designations", result.size());
    return result;
}
```

---

## Testing Your Logs

1. **Start the application**:
```bash
cd /home/sysadmin/data/projects/HRMS_New_Api
./mvnw spring-boot:run
```

2. **Open GraphiQL**:
```
http://localhost:8090/graphiql
```

3. **Run a query**:
```graphql
query {
  companies {
    id
    name
    code
  }
}
```

4. **Check console output** - You should see:
```
DEBUG - GraphQL Query: companies
DEBUG - Fetching all companies
INFO  - Service Response: getAllCompanies - returned 5 companies
INFO  - GraphQL Response: companies - returned 5 companies
```

5. **Check log file**:
```bash
tail -f /home/sysadmin/data/projects/HRMS_New_Api/logs/hrms-application.log
```

---

## Files Reference

### Configuration
- `src/main/resources/application.properties` - Logging configuration

### Documentation
- `GRAPHQL_LOGGING_IMPLEMENTATION_SUMMARY.md` - Complete documentation
- `LOGGING_QUICK_START.md` - This file

### Fully Implemented Examples
- `src/main/java/com/hrms/graphql/resolver/CompanyResolver.java` - Best example
- `src/main/java/com/hrms/graphql/resolver/EmployeeResolver.java` - Advanced patterns
- `src/main/java/com/hrms/graphql/resolver/UserAccountResolver.java` - Security-aware logging
- `src/main/java/com/hrms/graphql/resolver/DepartmentResolver.java` - Simple patterns

---

## Security Reminders

❌ **Never Log**:
- Passwords
- Session tokens (full value)
- API keys
- Sensitive PII

✅ **Safe to Log**:
- Entity IDs
- Entity names
- Counts
- Tenant IDs
- Operation results

---

## Need Help?

Refer to these files in the same order:
1. `LOGGING_QUICK_START.md` (this file) - Quick patterns
2. `GRAPHQL_LOGGING_IMPLEMENTATION_SUMMARY.md` - Complete documentation
3. `CompanyResolver.java` - Live working example

All patterns are consistent across the codebase!
