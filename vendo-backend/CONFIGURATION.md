# Vendo Backend Configuration Guide

## Database Configuration

The backend is configured to connect to PostgreSQL database `vendo_db` with the following credentials:

- **Database**: `vendo_db`
- **Host**: `localhost:5432`
- **Username**: `vendo_app`
- **Password**: `REDACTED_DB_PASSWORD`

### Configuration File
The database configuration is in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vendo_db
    username: ${DB_USERNAME:vendo_app}
    password: ${DB_PASSWORD:REDACTED_DB_PASSWORD}
```

### Environment Variables
You can override database settings using environment variables:

```bash
export DB_USERNAME=vendo_app
export DB_PASSWORD=REDACTED_DB_PASSWORD
export DB_HOST=localhost
export DB_PORT=5432
```

## Flyway Migration Configuration

Flyway is configured with `baseline-on-migrate: true`, which means:
- If the database already has tables (like ours does), Flyway will create a baseline
- It will then attempt to run any new migrations that haven't been applied
- Existing tables created manually will be preserved

### Current Status
- **Total Tables**: 85 tables created
- **Migration Files**: 62 migration files in `src/main/resources/db/migration/`
- **Flyway Schema History**: Will be created on first application startup

## Application Configuration

### Server
- **Port**: 8080
- **Base URL**: http://localhost:8080

### JWT Configuration
```yaml
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-must-be-at-least-32-characters-long-for-production}
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days
```

**Important**: Generate a secure JWT secret for production:
```bash
openssl rand -base64 32
```

### Redis Configuration (Optional)
Redis is used for caching. Default configuration:
- **Host**: localhost
- **Port**: 6379

If Redis is not running, the application will start but caching will be disabled.

### Kafka Configuration (Optional)
Kafka is used for async processing. Default configuration:
- **Bootstrap Servers**: localhost:9092

If Kafka is not running, the application will start but async features will be disabled.

## Starting the Application

### Prerequisites
1. PostgreSQL running on localhost:5432
2. Database `vendo_db` created
3. User `vendo_app` with correct password

### Start Script
```bash
./scripts/start.sh
```

### Manual Start
```bash
./gradlew bootRun
```

### Build Application
```bash
./scripts/build.sh
# or
./gradlew clean build
```

## Verification

### Verify Database Connection
```bash
./scripts/verify-database.sh
```

### Manual Verification
```bash
psql -h localhost -d vendo_db -U vendo_app -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';"
```

## Key Tables

The following key tables should exist:
- `users` - User accounts
- `ec_products` - Products catalog
- `ec_orders` - Orders
- `ec_cart` - Shopping carts
- `business_accounts` - B2B accounts
- `retailers` - Retailer accounts
- `payments` - Payment records
- `ec_customers` - Customer information

## Environment Variables Reference

### Required (with defaults)
- `DB_USERNAME` - Database username (default: vendo_app)
- `DB_PASSWORD` - Database password (default: REDACTED_DB_PASSWORD)
- `JWT_SECRET` - JWT signing secret (default: insecure, change for production)

### Optional
- `MAIL_HOST` - SMTP server host
- `MAIL_USERNAME` - SMTP username
- `MAIL_PASSWORD` - SMTP password
- `STRIPE_SECRET_KEY` - Stripe API key
- `S3_ACCESS_KEY` - AWS S3 access key
- `S3_SECRET_KEY` - AWS S3 secret key
- `GOOGLE_CLIENT_ID` - Google OAuth client ID
- `GOOGLE_CLIENT_SECRET` - Google OAuth client secret
- And many more (see application.yml for full list)

## Troubleshooting

### Database Connection Issues
1. Verify PostgreSQL is running: `pg_isready -h localhost -p 5432`
2. Check database exists: `psql -h localhost -d vendo_db -U vendo_app -c "SELECT 1;"`
3. Verify user permissions: `psql -h localhost -d postgres -c "\du vendo_app"`

### Flyway Migration Issues
- If migrations fail, check `flyway_schema_history` table
- Flyway will skip migrations that already exist (IF NOT EXISTS)
- To reset: Drop and recreate database, then restart application

### Port Already in Use
If port 8080 is already in use, change it in `application.yml`:
```yaml
server:
  port: 8081  # Change to available port
```

## Next Steps

1. **Generate JWT Secret**: Create a secure JWT secret for production
2. **Configure Email**: Set up SMTP for email notifications
3. **Configure Storage**: Set up S3 or MinIO for file storage
4. **Configure Payment Gateways**: Add API keys for payment processing
5. **Start Application**: Run `./scripts/start.sh` to start the backend

