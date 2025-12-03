# Troubleshooting

## Build Issues

### Build Fails: "Cannot find symbol"

**Cause**: Incomplete Maven build or stale classes

**Solution**:
```bash
mvn clean compile
# or
mvn clean install -DskipTests
```

**If still fails**:
- Delete `target/` folder manually: `rm -rf target/`
- Check Java version: `java -version` (must be 17+)
- Check Maven: `mvn -v` (must be 3.9.6+)

### Slow Build (First Time)

**Cause**: Maven downloading dependencies for first time

**Solution**:
- Patience required (5-10 minutes normal)
- Check internet connectivity
- If stuck > 15 min: `Ctrl+C` and retry

**Speed Up**:
```bash
export MAVEN_OPTS="-Xmx1024m"
mvn -o clean compile  # Use cached dependencies only
```

### "ProjectBuilder" Errors

**Cause**: Corrupted Maven cache

**Solution**:
```bash
rm -rf ~/.m2/repository
mvn clean install -DskipTests
```

## Database Issues

### PostgreSQL Connection Refused

**Error**: `Could not get a connection, pool error`

**Diagnosis**:
```bash
# Check PostgreSQL running
psql -U postgres -d postgres -c "SELECT 1"

# Check port listening
netstat -an | grep 5432
# or
lsof -i :5432
```

**Solutions**:
1. Start PostgreSQL:
   - macOS: `brew services start postgresql`
   - Linux: `sudo systemctl start postgresql`
   - Windows: Start from Services control panel

2. Check credentials in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/hrmsdb
   spring.datasource.username=postgres
   spring.datasource.password=Admin@123
   ```

3. Create database if missing:
   ```bash
   psql -U postgres
   CREATE DATABASE hrmsdb;
   \q
   ```

### Table/Schema Not Created

**Error**: `org.hibernate.ObjectNotFoundException` or `relation does not exist`

**Cause**: Hibernate DDL not executed (usually on first run)

**Solution**:
1. Verify Hibernate setting: `spring.jpa.hibernate.ddl-auto=update` in properties
2. Restart application (schema created on startup)
3. Check PostgreSQL logs: `tail -f /usr/local/var/log/postgres.log`

**Manual Schema Creation**:
```bash
# Hibernate didn't create schema, create manually
psql -U postgres hrmsdb
CREATE SCHEMA IF NOT EXISTS public;
GRANT ALL PRIVILEGES ON SCHEMA public TO postgres;
\q
# Restart app
```

### Data Initialization Failed

**Error**: `DataIntegrityViolationException` during startup

**Cause**: DataInitializer inserting duplicate data

**Solution**:
```bash
# Clear test data
psql -U postgres hrmsdb
TRUNCATE TABLE employees CASCADE;
TRUNCATE TABLE companies CASCADE;
TRUNCATE TABLE users CASCADE;
\q
# Restart app (DataInitializer repopulates)
```

### Indexes Not Being Used

**Error**: Slow queries despite indexes

**Diagnosis**:
```sql
-- Check if indexes exist
SELECT * FROM pg_indexes WHERE tablename = 'employees';

-- Check query plan
EXPLAIN ANALYZE SELECT * FROM employees WHERE tenant_id = '1';
```

**Solution**:
```sql
-- Recreate indexes
DROP INDEX IF EXISTS idx_employee_tenant;
CREATE INDEX idx_employee_tenant ON employees(tenant_id);

-- Analyze table statistics
ANALYZE employees;
```

## Application Startup Issues

### Port 8090 Already in Use

**Error**: `Address already in use`

**Solution**:
```bash
# Find process using port
lsof -i :8090

# Kill it
kill -9 <PID>

# Or change port in application.properties
server.port=8091
```

### Application Hangs on Startup

**Cause**: Waiting for database connection (slow startup normal first time)

**Diagnosis**:
- Wait 1-2 minutes (DataInitializer populates data)
- Check logs: `tail -f logs/application.log`
- Look for "Started HrmsApplication" message

**If truly stuck**:
- Kill process: `Ctrl+C`
- Check PostgreSQL: `psql -U postgres -c "SELECT 1"`
- Increase connection timeout:
  ```properties
  spring.datasource.hikari.connection-timeout=60000
  ```

### Hibernate Warning: "No Dialect Set"

**Error**: Hibernate warning but app still runs

**Solution** (optional):
```properties
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL12Dialect
spring.jpa.hibernate.ddl-auto=update
```

## Authentication Issues

### JWT Token Invalid/Expired

**Error**: `Unauthenticated` or `Token has expired`

**Cause**: Token older than 8 hours

**Solution**:
- Re-login to get new token
- Or use refresh token to get new access token:
  ```bash
  POST /auth/refresh
  Content-Type: application/json

  { "refreshToken": "..." }
  ```

### Login Fails: Invalid Credentials

**Error**: `Unauthorized` on login attempt

**Diagnosis**:
```bash
# Check user exists
psql -U postgres hrmsdb
SELECT username, password_hash FROM users WHERE username = 'admin';
```

**Solution**:
1. Verify username/password correct
2. Reset password (if available)
3. Check user status: `SELECT account_status FROM users WHERE username = 'admin';`
4. If locked: Update status: `UPDATE users SET account_status = 'ACTIVE' WHERE username = 'admin';`

### Missing Authorization Header

**Error**: `401 Unauthorized` on GraphQL requests

**Solution**:
- Ensure Authorization header included:
  ```
  Authorization: Bearer eyJhbGc...
  ```
- Use GraphQL IDE (GraphiQL) which handles this automatically

### Scope Violation: "Unauthorized Access"

**Error**: `FORBIDDEN` or `UNAUTHENTICATED` when querying data

**Cause**: User doesn't have scope for requested data

**Diagnosis**:
```bash
# Check user's scope
psql -U postgres hrmsdb
SELECT * FROM user_organizational_scope WHERE user_id = 'john';

# Check what they're requesting
# vs what's authorized
```

**Solution**:
- Admin must assign proper scope: `UPDATE user_organizational_scope SET company_id = 1 WHERE user_id = 'john';`
- Or request data within authorized scope

## GraphQL Issues

### GraphQL Query Syntax Error

**Error**: `Parsing error` or `Variable not valid`

**Solution**:
- Use GraphQL IDE (GraphiQL) at `/graphiql` - it validates syntax
- Check query structure matches schema (use IDE autocomplete)
- Common mistake: Missing colons in input objects

### Empty Response from Query

**Error**: Query succeeds but returns `null` or `[]`

**Cause**: Data doesn't match filters or doesn't exist

**Diagnosis**:
```bash
# Check if data exists in database
psql -U postgres hrmsdb
SELECT COUNT(*) FROM employees;

# Check what query is being executed
# Look at application.log for SQL statements (if logging enabled)
```

**Solution**:
- Insert test data: Use DataInitializer or GraphQL mutations
- Relax filters: Query without filters first
- Check user's organizational scope allows requested data

### GraphQL Timeout

**Error**: `Request timeout` or GraphQL query hangs

**Cause**: Slow query or large result set

**Solution**:
1. Add pagination:
   ```graphql
   employees(filter: { page: 0, size: 20 }) { ... }
   ```

2. Add timeout configuration:
   ```properties
   spring.graphql.execution.timeout=30s
   ```

3. Check database performance:
   ```sql
   SELECT * FROM pg_stat_statements ORDER BY mean_time DESC LIMIT 5;
   ```

## Performance Issues

### High CPU Usage

**Diagnosis**:
```bash
# Check Java processes
jps -l

# Check thread count
jstack <PID> | grep "tid"

# Check GC activity
jstat -gc <PID> 1000  # Every 1 second
```

**Solutions**:
- Increase JVM heap: `java -Xmx2g -jar app.jar`
- Check slow queries: Enable query logging in application.properties
- Add database indexes: See database section above

### High Memory Usage

**Error**: `OutOfMemoryError: Java heap space`

**Diagnosis**:
```bash
# Check heap size
jps -l -m

# Dump heap
jmap -dump:live,format=b,file=heap.bin <PID>
```

**Solutions**:
- Increase JVM heap: `java -Xmx4g -jar app.jar`
- Reduce batch size in queries (use pagination)
- Check for memory leaks: Analyze heap dump

### Slow GraphQL Queries

**Diagnosis**:
1. Check logs for query execution time
2. Enable query logging:
   ```properties
   logging.level.org.hibernate.SQL=DEBUG
   logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
   ```

3. Profile query:
   ```sql
   EXPLAIN ANALYZE SELECT * FROM employees WHERE tenant_id = '1' LIMIT 20;
   ```

**Solutions**:
- Add indexes: See database troubleshooting
- Use pagination: `page: 0, size: 20` instead of fetching all
- Reduce SELECT fields: Only fetch needed columns
- Use N+1 query detection tools

## Logging & Debugging

### Logs Not Appearing

**Cause**: Logging level too high or logs to wrong location

**Solution**:
```properties
# Set to DEBUG for development
logging.level.root=DEBUG
logging.level.com.hrms=DEBUG

# Check log location
logging.file.name=logs/application.log
```

**View logs**:
```bash
tail -f logs/application.log
grep "GraphQL" logs/application.log
```

### Too Many Logs (Spam)

**Cause**: DEBUG logging enabled for verbose libraries

**Solution**:
```properties
logging.level.root=INFO
logging.level.com.hrms=DEBUG
logging.level.org.springframework.web=WARN
logging.level.org.hibernate=WARN
```

### Enable SQL Query Logging

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

## Testing Issues

### Tests Fail with Database Connection Error

**Solution**:
- Ensure PostgreSQL running: `pg_isready -h localhost -p 5432`
- Or use in-memory H2 for tests (configure test properties)

### Test Data Not Persisting Between Tests

**Cause**: Transaction rollback after each test

**Solution** (if needed):
```java
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MyTest { ... }
```

## Integration Issues

### Frontend Can't Connect to Backend

**Error**: CORS error or connection refused

**Diagnosis**:
- Backend running: `curl -X GET http://localhost:8090/graphql -I`
- Check CORS allowed origins in application.properties
- Check frontend calling correct URL

**Solution**:
```properties
# In application.properties
corsFilter.allowedOrigins=http://localhost:3000,http://localhost:5173
```

### GraphQL Schema Not Updating

**Cause**: IDE cached schema

**Solution**:
- Refresh GraphQL schema: IDE command or restart
- Restart GraphQL client
- Clear cache: `rm -rf .apollo/` (if using Apollo)

## Contact & Escalation

**For issues not covered**:
1. Check `logs/application.log` for full error
2. Verify database connectivity
3. Review schema: `SELECT * FROM information_schema.tables;`
4. Check application properties match environment

---
**Status**: Common issues documented. Refer to logs for non-listed errors.
