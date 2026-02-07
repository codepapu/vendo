# Vendo Backend Deployment Guide

This guide provides comprehensive instructions for deploying the Vendo e-commerce platform backend to production.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Database Setup](#database-setup)
4. [Application Configuration](#application-configuration)
5. [Building the Application](#building-the-application)
6. [Deployment Options](#deployment-options)
7. [Docker Deployment](#docker-deployment)
8. [Cloud Deployment](#cloud-deployment)
9. [Monitoring and Logging](#monitoring-and-logging)
10. [Backup and Recovery](#backup-and-recovery)
11. [Security Checklist](#security-checklist)
12. [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Software

- **Java 21+** - JDK 21 or later
- **Maven 3.8+** - Build tool
- **PostgreSQL 14+** - Database
- **Redis 6+** - Caching and session management
- **Elasticsearch 8+** (optional) - Search functionality
- **Kafka 2.13+** (optional) - Event streaming
- **MinIO or AWS S3** - File storage

### Required Services (Optional)

- **Email Service** - For transactional emails (SendGrid, AWS SES, etc.)
- **SMS Service** - For OTP and notifications (Twilio, AWS SNS)
- **Push Notification Service** - FCM (Android) and APNS (iOS)
- **Payment Gateways** - Stripe, PayPal, Razorpay, etc.

## Environment Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd vendo/vendo-backend
```

### 2. Set Environment Variables

Create a `.env` file or set environment variables:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/vendo
SPRING_DATASOURCE_USERNAME=vendo_user
SPRING_DATASOURCE_PASSWORD=secure_password

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=redis_password

# JWT
JWT_SECRET=your-secret-key-min-256-bits
JWT_EXPIRATION=86400000

# File Storage
APP_STORAGE_TYPE=s3
APP_STORAGE_S3_ACCESS_KEY=your_access_key
APP_STORAGE_S3_SECRET_KEY=your_secret_key
APP_STORAGE_S3_BUCKET=vendo-uploads
APP_STORAGE_S3_REGION=us-east-1

# Elasticsearch (optional)
SPRING_ELASTICSEARCH_URIS=http://localhost:9200

# Kafka (optional)
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Email
SPRING_MAIL_HOST=smtp.sendgrid.net
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=apikey
SPRING_MAIL_PASSWORD=your_sendgrid_api_key

# Payment Gateways
PAYMENT_STRIPE_SECRET_KEY=sk_live_...
PAYMENT_STRIPE_WEBHOOK_SECRET=whsec_...
PAYMENT_PAYPAL_CLIENT_ID=your_paypal_client_id
PAYMENT_PAYPAL_CLIENT_SECRET=your_paypal_client_secret

# SMS (optional)
APP_SMS_PROVIDER=twilio
APP_SMS_TWILIO_ACCOUNT_SID=your_account_sid
APP_SMS_TWILIO_AUTH_TOKEN=your_auth_token
APP_SMS_TWILIO_FROM_NUMBER=+1234567890

# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

## Database Setup

### 1. Create Database

```sql
CREATE DATABASE vendo;
CREATE USER vendo_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE vendo TO vendo_user;
```

### 2. Run Migrations

The application uses Flyway for database migrations. Migrations run automatically on startup.

To run migrations manually:

```bash
mvn flyway:migrate
```

### 3. Verify Schema (Tables and Columns)

After migrations, verify that all expected tables exist:

```bash
# Quick check (connection, table count, expected tables from expected-tables.txt)
./scripts/verify-database.sh

# Strict check: exit 1 if any expected table is missing (e.g. for CI)
./scripts/verify-schema.sh

# Also verify key columns on critical tables
./scripts/verify-schema.sh --columns
```

Expected tables are listed in `scripts/expected-tables.txt` (one per line). Update that file when adding new migrations.

### 4. Verify Database Connection

```bash
psql -h localhost -U vendo_user -d vendo -c "SELECT version();"
```

(If using default setup: database `vendo_db`, user `vendo_app`; see `scripts/verify-database.sh`.)

## Application Configuration

### Production Profile

Create `application-prod.properties`:

```properties
# Server
server.port=8080
server.compression.enabled=true

# Database
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:vendo}
spring.datasource.username=${DB_USERNAME:vendo_user}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Redis
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.password=${REDIS_PASSWORD:}

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

# File Storage
app.storage.type=${STORAGE_TYPE:s3}
app.storage.s3.access-key=${S3_ACCESS_KEY}
app.storage.s3.secret-key=${S3_SECRET_KEY}
app.storage.s3.bucket=${S3_BUCKET:vendo-uploads}
app.storage.s3.region=${S3_REGION:us-east-1}

# Logging
logging.level.com.vendo=INFO
logging.level.org.springframework=WARN
logging.file.name=/var/log/vendo/application.log
logging.file.max-size=10MB
logging.file.max-history=30

# Security
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
```

## Building the Application

### 1. Build JAR

```bash
mvn clean package -DskipTests
```

The JAR file will be created at:
```
target/vendo-1.0.0-SNAPSHOT.jar
```

### 2. Run Application

```bash
java -jar target/vendo-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

Or with environment variables:

```bash
java -jar \
  -Dspring.profiles.active=prod \
  -Dspring.datasource.password=${DB_PASSWORD} \
  -Djwt.secret=${JWT_SECRET} \
  target/vendo-1.0.0-SNAPSHOT.jar
```

## Deployment Options

### Option 1: Systemd Service (Linux)

Create `/etc/systemd/system/vendo.service`:

```ini
[Unit]
Description=Vendo E-commerce Backend
After=network.target postgresql.service redis.service

[Service]
Type=simple
User=vendo
WorkingDirectory=/opt/vendo
ExecStart=/usr/bin/java -jar -Xmx2048m -Xms1024m /opt/vendo/vendo-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

Enable and start:

```bash
sudo systemctl enable vendo
sudo systemctl start vendo
sudo systemctl status vendo
```

### Option 2: Docker Deployment

See [Docker Deployment](#docker-deployment) section.

### Option 3: Cloud Platform

- **AWS**: Use Elastic Beanstalk, ECS, or EC2
- **Google Cloud**: Use Cloud Run or GKE
- **Azure**: Use App Service or AKS
- **DigitalOcean**: Use App Platform or Droplets

## Docker Deployment

### 1. Build Docker Image

Create `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/vendo-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Xmx2048m", "-Xms1024m", "app.jar"]
```

Build:

```bash
docker build -t vendo-backend:latest .
```

### 2. Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    environment:
      POSTGRES_DB: vendo
      POSTGRES_USER: vendo_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"

  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/vendo
      SPRING_DATASOURCE_USERNAME: vendo_user
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PASSWORD: ${REDIS_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      - postgres
      - redis

volumes:
  postgres_data:
  redis_data:
```

Run:

```bash
docker-compose up -d
```

## Cloud Deployment

### AWS Elastic Beanstalk

1. Install EB CLI:
```bash
pip install awsebcli
```

2. Initialize:
```bash
eb init -p java-21 vendo-backend
```

3. Create environment:
```bash
eb create vendo-prod
```

4. Set environment variables:
```bash
eb setenv SPRING_PROFILES_ACTIVE=prod DB_PASSWORD=... JWT_SECRET=...
```

5. Deploy:
```bash
eb deploy
```

### Google Cloud Run

1. Build and push:
```bash
gcloud builds submit --tag gcr.io/PROJECT_ID/vendo-backend
```

2. Deploy:
```bash
gcloud run deploy vendo-backend \
  --image gcr.io/PROJECT_ID/vendo-backend \
  --platform managed \
  --region us-central1 \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod
```

## Monitoring and Logging

### Application Logs

Logs are configured to write to:
- `/var/log/vendo/application.log` (file)
- Standard output (for containerized deployments)

### Health Checks

The application exposes health endpoints:
- `/actuator/health` - Basic health check
- `/actuator/info` - Application info

### Metrics (Optional)

Enable Actuator metrics:
```properties
management.endpoints.web.exposure.include=health,info,metrics
management.metrics.export.prometheus.enabled=true
```

## Backup and Recovery

### Database Backup

#### Automated Backup Script

Create `scripts/backup-db.sh`:

```bash
#!/bin/bash
BACKUP_DIR="/var/backups/vendo"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

pg_dump -h localhost -U vendo_user -d vendo | gzip > $BACKUP_DIR/vendo_$DATE.sql.gz

# Keep only last 30 days
find $BACKUP_DIR -name "vendo_*.sql.gz" -mtime +30 -delete
```

#### Restore

```bash
gunzip < backup.sql.gz | psql -h localhost -U vendo_user -d vendo
```

### File Storage Backup

For S3, enable versioning and cross-region replication.

For MinIO, use `mc mirror`:

```bash
mc mirror --remove local/minio/bucket s3/backup-bucket
```

## Security Checklist

- [ ] Use strong database passwords
- [ ] Use strong JWT secret (min 256 bits)
- [ ] Enable HTTPS/TLS
- [ ] Configure CORS properly
- [ ] Set secure cookie flags
- [ ] Enable rate limiting
- [ ] Configure firewall rules
- [ ] Keep dependencies updated
- [ ] Use environment variables for secrets
- [ ] Enable database SSL connections
- [ ] Configure Redis authentication
- [ ] Set up proper backup strategy
- [ ] Configure log rotation
- [ ] Enable security headers
- [ ] Review and restrict API endpoints
- [ ] Set up monitoring and alerting

## Troubleshooting

### Application Won't Start

1. Check Java version:
```bash
java -version  # Should be 21+
```

2. Check database connection:
```bash
psql -h localhost -U vendo_user -d vendo
```

3. Check logs:
```bash
tail -f /var/log/vendo/application.log
```

### Database Connection Issues

1. Verify database is running:
```bash
sudo systemctl status postgresql
```

2. Check connection settings in `application.properties`

3. Verify network connectivity:
```bash
telnet localhost 5432
```

### Out of Memory Errors

Increase JVM heap size:
```bash
java -Xmx4096m -Xms2048m -jar app.jar
```

### Slow Performance

1. Check database indexes
2. Verify Redis connectivity
3. Review query performance
4. Check application logs for errors
5. Monitor CPU and memory usage

## Production Checklist

Before deploying to production:

- [ ] All environment variables configured
- [ ] Database migrations tested
- [ ] SSL/TLS certificates configured
- [ ] Backup strategy in place
- [ ] Monitoring and alerting set up
- [ ] Security audit completed
- [ ] Performance testing done
- [ ] Load testing completed
- [ ] Disaster recovery plan documented
- [ ] Documentation updated

## Support

For issues and questions:
- Check logs: `/var/log/vendo/application.log`
- Review application documentation
- Contact the development team

