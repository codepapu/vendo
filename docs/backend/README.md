# Vendo E-commerce Platform

**Author:** Papu Sahu (code.papu@gmail.com)  
**Version:** 1.0.0-SNAPSHOT  
**Framework:** Spring Boot 3.4.0

---

## Overview

Vendo is a modern, monolithic e-commerce platform built with Spring Boot. It provides a complete solution for online retail, marketplace, and multi-vendor commerce.

---

## Architecture

This is a **monolithic Spring Boot application** that consolidates all e-commerce functionality into a single deployable application.

### Features

- ✅ User Management & Authentication (JWT, OAuth2, Social Login)
- ✅ Product Catalog with Categories, Variations, Attributes
- ✅ Shopping Cart & Checkout
- ✅ Order Management & Tracking
- ✅ Payment Processing (Multiple Gateways)
- ✅ Multi-vendor Marketplace
- ✅ Content Management (Blog, Pages, Media)
- ✅ Multi-channel Notifications (Email, SMS, Push, WebSocket)
- ✅ Analytics & Reporting
- ✅ Admin Dashboard
- ✅ Event-Driven Architecture (Kafka)
- ✅ Saga Pattern for Distributed Transactions

---

## Quick Start

### Prerequisites

- Java 21+
- PostgreSQL 16+ (localhost:5432)
- Redis (localhost:6379) - Optional but recommended
- Kafka (localhost:9092) - Optional for async processing
- Gradle (or use Gradle wrapper)

### Installation

1. **Install Local Services:**

   **macOS:**
   ```bash
   brew install postgresql@16 redis kafka
   brew services start postgresql@16
   brew services start redis
   brew services start zookeeper
   brew services start kafka
   ```

   **Linux:**
   ```bash
   sudo apt-get install postgresql-16 redis-server
   # Install Kafka from https://kafka.apache.org/downloads
   sudo systemctl start postgresql
   sudo systemctl start redis
   ```

2. **Initialize Database:**
   ```bash
   ./scripts/init-databases.sh
   ```

3. **Build and Run:**
   ```bash
   ./gradlew build
   ./gradlew bootRun
   ```

   Or use the Gradle wrapper:
   ```bash
   ./gradlew bootRun
   ```

---

## Application URLs

- **Application:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/api-docs
- **Health Check:** http://localhost:8080/actuator/health

---

## Technology Stack

- **Framework:** Spring Boot 3.4.0
- **Language:** Java 21
- **Build Tool:** Gradle
- **Database:** PostgreSQL 16
- **Cache:** Redis, Caffeine
- **Message Broker:** Apache Kafka (optional)
- **Security:** Spring Security, OAuth2, JWT
- **Documentation:** OpenAPI/Swagger
- **Migration:** Flyway

---

## Project Structure

```
vendo-backend/
├── src/
│   ├── main/
│   │   ├── java/com/vendo/
│   │   │   ├── user/          # User management
│   │   │   ├── product/       # Product catalog
│   │   │   ├── order/         # Order processing
│   │   │   ├── payment/       # Payment processing
│   │   │   ├── cart/          # Shopping cart
│   │   │   ├── marketplace/   # Marketplace features
│   │   │   ├── content/       # Content management
│   │   │   ├── notification/  # Notifications
│   │   │   ├── analytics/     # Analytics
│   │   │   ├── admin/         # Admin dashboard
│   │   │   └── common/        # Shared utilities
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/  # Flyway migrations
│   └── test/
├── build.gradle.kts
├── settings.gradle.kts
└── scripts/
```

---

## Configuration

All services are configured in `src/main/resources/application.yml`:

- **PostgreSQL:** `localhost:5432`
- **Database:** `vendo_db`
- **Redis:** `localhost:6379`
- **Kafka:** `localhost:9092` (optional)

### Environment Variables

Key environment variables you can override:

- `JWT_SECRET`: JWT signing secret
- `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`: Email configuration
- `S3_BUCKET`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`: S3 storage configuration
- `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`: Google OAuth
- `FACEBOOK_CLIENT_ID`, `FACEBOOK_CLIENT_SECRET`: Facebook OAuth
- `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`: GitHub OAuth

---

## Migration from Microservices

If you're migrating from the microservices version, see `MONOLITHIC_MIGRATION.md` for detailed instructions.

---

## Development

### Running Tests

```bash
./gradlew test
```

### Building

```bash
./gradlew build
```

### Running

```bash
./gradlew bootRun
```

---

## Documentation

- **MONOLITHIC_MIGRATION.md** - Migration guide from microservices
- **API Documentation:** http://localhost:8080/swagger-ui.html

---

## Author

**Papu Sahu** (code.papu@gmail.com)

---

## License

Proprietary - All rights reserved

---

**Vendo E-commerce Platform** - Modern, scalable, monolithic e-commerce solution.
