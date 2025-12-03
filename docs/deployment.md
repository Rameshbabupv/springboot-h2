# Deployment

## Application Package

**Artifact**: `hrms-saas-1.0.0.jar` (~50MB)

Single executable JAR containing:
- All dependencies packaged (fat JAR)
- Embedded Spring Boot server
- PostgreSQL JDBC driver
- Configuration defaults (overrideable)

**Build**:
```bash
mvn clean package
```

## Target Environments

### Development (localhost)
- Port: 8090
- Database: PostgreSQL localhost:5432
- Logging: Console + file
- Security: Basic (localhost CORS allowed)

### Staging
- Port: 8090 (or container port)
- Database: PostgreSQL staging instance
- Logging: File with rotation
- Security: SSL enabled, restricted CORS

### Production
- Port: 8090 or behind load balancer (port 80/443)
- Database: PostgreSQL production instance (with replication)
- Logging: Centralized logging (ELK stack recommended)
- Security: SSL mandatory, hardened CORS, secrets externalized

## Configuration Management

### Environment-Specific Properties

Create property files per environment:
- `application.properties` (defaults)
- `application-dev.properties`
- `application-staging.properties`
- `application-prod.properties`

**Activate**:
```bash
java -jar app.jar --spring.profiles.active=prod
```

### Key Properties to Override

| Property | Development | Production |
|----------|-------------|-----------|
| server.port | 8090 | 8090 |
| spring.datasource.url | localhost:5432 | prod-db-host:5432 |
| spring.datasource.username | postgres | (from env var) |
| spring.datasource.password | Admin@123 | (from env var) |
| spring.jpa.hibernate.ddl-auto | update | validate |
| logging.level.root | DEBUG | INFO |
| server.servlet.context-path | / | /api |
| corsFilter.allowedOrigins | localhost:3000 | prod-frontend.com |

### Secrets Externalization

**Never commit**:
- Database passwords
- JWT signing key
- API keys
- OAuth credentials

**Use environment variables**:
```bash
export DB_PASSWORD=<secret>
export JWT_SECRET=<secret>
export CORS_ORIGIN=https://frontend.com

java -jar app.jar \
  --spring.datasource.password=$DB_PASSWORD \
  --app.jwt.secret=$JWT_SECRET
```

Or Docker secrets / Kubernetes secrets for container deployments.

## Database Deployment

### PostgreSQL Setup

**Production Requirements**:
- PostgreSQL 12+ (newer versions preferred)
- Dedicated database instance
- Separate user account (not postgres superuser)
- Connection pooling enabled (HikariCP default: 10 connections)

**Database Creation**:
```sql
CREATE DATABASE hrmsdb_prod;
CREATE ROLE hrms_app WITH PASSWORD 'secure-password';
ALTER ROLE hrms_app CREATEDB;
GRANT CONNECT ON DATABASE hrmsdb_prod TO hrms_app;
GRANT USAGE ON SCHEMA public TO hrms_app;
GRANT CREATE ON SCHEMA public TO hrms_app;
```

### Schema Management

**Hibernate DDL Auto**:
- Development: `update` (schema auto-updates)
- Production: `validate` (schema must pre-exist)

**Production Migration Strategy**:
1. Test schema updates on staging first
2. Use database migration tools (Flyway/Liquibase) for tracked changes
3. Validate schema on startup via `validate` mode
4. Keep schema backups before major updates

**Manual Migration Example**:
```bash
# Export current schema
pg_dump --schema-only hrmsdb > schema.sql

# Apply to production
psql -U hrms_app -d hrmsdb_prod -f schema.sql
```

### Backup Strategy

**Daily Backups**:
```bash
pg_dump hrmsdb_prod | gzip > backup_$(date +%Y%m%d).sql.gz
```

**Restore**:
```bash
gunzip < backup_20251203.sql.gz | psql hrmsdb_prod
```

**Retention**: 30-day rolling backups + 1 full backup per quarter

### High Availability

**Replication** (optional):
- Primary-Replica setup for read scaling
- Connection pooling routes reads to replica
- Writes always to primary

**Failover** (optional):
- Patroni/etcd for automatic failover
- Health checks every 10 seconds
- Replica promotes to primary on primary failure

## Deployment Strategies

### Traditional Server Deployment

```bash
# On target server
wget https://repo/hrms-saas-1.0.0.jar
java -jar hrms-saas-1.0.0.jar \
  --spring.profiles.active=prod \
  --spring.datasource.password=$DB_PASSWORD
```

**Systemd Service** (Linux):
```ini
[Unit]
Description=HRMS SaaS Application
After=network.target

[Service]
Type=simple
User=hrmsapp
ExecStart=/usr/bin/java -jar /opt/app/hrms-saas-1.0.0.jar \
  --spring.profiles.active=prod \
  --spring.datasource.password=${DB_PASSWORD}
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Start service:
```bash
systemctl start hrms-saas
systemctl status hrms-saas
journalctl -u hrms-saas -f
```

### Docker Deployment

**Dockerfile**:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/hrms-saas-1.0.0.jar app.jar
EXPOSE 8090
CMD ["java", "-jar", "app.jar"]
```

**Build & Push**:
```bash
docker build -t hrms-saas:1.0.0 .
docker tag hrms-saas:1.0.0 registry/hrms-saas:1.0.0
docker push registry/hrms-saas:1.0.0
```

**Run Container**:
```bash
docker run -d \
  -p 8090:8090 \
  -e DB_PASSWORD=secret \
  -e JWT_SECRET=secret \
  -e CORS_ORIGIN=https://frontend.com \
  --name hrms-api \
  hrms-saas:1.0.0
```

### Kubernetes Deployment

**Deployment YAML**:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hrms-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: hrms-api
  template:
    metadata:
      labels:
        app: hrms-api
    spec:
      containers:
      - name: hrms-api
        image: registry/hrms-saas:1.0.0
        ports:
        - containerPort: 8090
        env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: hrms-secrets
              key: db_password
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        resources:
          requests:
            cpu: 500m
            memory: 512Mi
          limits:
            cpu: 1000m
            memory: 1Gi
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8090
          initialDelaySeconds: 30
          periodSeconds: 10
```

**Service** (expose to load balancer):
```yaml
apiVersion: v1
kind: Service
metadata:
  name: hrms-api-service
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8090
  selector:
    app: hrms-api
```

Deploy:
```bash
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```

## Monitoring & Observability

### Health Checks

**Endpoint**: `GET /actuator/health`

Response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "livenessState": { "status": "UP" },
    "readinessState": { "status": "UP" }
  }
}
```

**Load Balancer Configuration**: Route health checks to this endpoint (no auth required)

### Logging

**Aggregation** (Production Recommended):
- Ship logs to centralized system (ELK Stack, Splunk, DataDog)
- Structured logging: Convert logs to JSON for better parsing

**Log Rotation**:
- Logback configured for daily rotation
- Keep 30 days of history
- Compress old logs to save disk space

**Configuration**:
```properties
logging.file.name=logs/application.log
logging.file.max-size=10MB
logging.file.max-history=30
logging.file.total-size-cap=5GB
```

### Metrics

**Expose metrics** via Actuator:
```
GET /actuator/metrics
GET /actuator/metrics/http.server.requests
```

**Integration**:
- Prometheus scraping: `/actuator/prometheus`
- Grafana dashboards for visualization
- Alert on: High latency, error rates, database connection pool exhaustion

### Performance Monitoring

**Key Metrics**:
- HTTP request latency (p50, p95, p99)
- GraphQL query execution time
- Database query time
- JVM heap usage
- Database connection pool usage

## Scaling

### Vertical Scaling
- Increase JVM heap: `java -Xmx2g -Xms2g -jar app.jar`
- Increase database connections: `hikari.maximum-pool-size=50`

### Horizontal Scaling
- Deploy multiple instances (Docker/Kubernetes)
- Use load balancer (nginx, AWS ALB)
- Ensure stateless design (JWT-based, no session affinity needed)
- Database becomes bottleneck (use read replicas for scaling)

### Database Connection Pooling

**HikariCP Configuration**:
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
```

**Tuning**: Monitor connection usage, increase if hitting limits

## Rollback & Disaster Recovery

### Blue-Green Deployment

```
Blue (current):   v1.0 running on servers A, B, C
Green (new):      v1.1 deployed on servers D, E, F
Traffic:          Switched from Blue to Green
If issue:         Switch back to Blue (fast rollback)
```

### Canary Deployment

```
Baseline:         v1.0 on all servers
Canary:           v1.1 on 10% servers, monitor metrics
If healthy:       Gradually increase to 25% → 50% → 100%
If issues:        Rollback 10%, investigate, retry
```

### Database Rollback

**Pre-Deployment**:
1. Backup current database
2. Test migration on staging
3. Plan rollback steps

**Post-Deployment Issues**:
1. Restore from backup: `psql < backup_pre_deploy.sql`
2. Revert to previous JAR version
3. Test on staging, then redeploy to production

## Post-Deployment Verification

**Checklist**:
- [ ] Application starts without errors
- [ ] Health check returns UP
- [ ] Database connectivity verified
- [ ] GraphQL endpoint responds
- [ ] Authentication works (login returns token)
- [ ] Sample query returns data
- [ ] Organizational scope filtering works
- [ ] Logs appear in correct location
- [ ] Monitoring/alerts configured
- [ ] Backup jobs scheduled

---
**Status**: Deployment ready for staging/production. Kubernetes templates provided.
