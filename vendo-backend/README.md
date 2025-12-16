# Nexus E-commerce Platform

**Author:** code.papu@gmail.com  
**Version:** 1.0.0-SNAPSHOT  
**Framework:** Spring Boot 3.3.0

---

## Overview

Nexus is a modern, microservices-based e-commerce platform built with Spring Boot. It provides a complete solution for online retail, marketplace, and multi-vendor commerce.

---

## Architecture

### Microservices

- **nexus-discovery-service** - Service Discovery (Eureka)
- **nexus-config-service** - Configuration Server
- **nexus-auth-service** - OAuth2 Authorization Server
- **nexus-api-gateway** - API Gateway
- **nexus-user-service** - User Management & Authentication
- **nexus-product-service** - Product Catalog Management
- **nexus-order-service** - Order Processing
- **nexus-payment-service** - Payment Processing
- **nexus-cart-service** - Shopping Cart Management
- **nexus-marketplace-service** - Multi-vendor Marketplace
- **nexus-content-service** - Content Management (Blog, Media)
- **nexus-notification-service** - Multi-channel Notifications
- **nexus-analytics-service** - Analytics & Reporting
- **nexus-admin-service** - Admin Dashboard & Management
- **nexus-common** - Shared Common Module

---

## Quick Start

### Prerequisites

- Java 21+
- PostgreSQL 16+ (localhost:5432)
- Redis (localhost:6379)
- Kafka (localhost:9092) - Optional but recommended
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

3. **Start All Services:**
   ```bash
   ./scripts/start-all.sh
   ```

---

## Service URLs

- **API Gateway:** http://localhost:8080
- **Discovery Service:** http://localhost:8761
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **User Service:** http://localhost:8081
- **Product Service:** http://localhost:8082
- **Order Service:** http://localhost:8083
- **Payment Service:** http://localhost:8084
- **Content Service:** http://localhost:8085
- **Notification Service:** http://localhost:8086
- **Analytics Service:** http://localhost:8087
- **Admin Service:** http://localhost:8088
- **Cart Service:** http://localhost:8089
- **Marketplace Service:** http://localhost:8090

---

## Features

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

## Technology Stack

- **Framework:** Spring Boot 3.3.0
- **Language:** Java 21
- **Build Tool:** Gradle
- **Database:** PostgreSQL 16
- **Cache:** Redis
- **Message Broker:** Apache Kafka
- **Service Discovery:** Netflix Eureka
- **API Gateway:** Spring Cloud Gateway
- **Security:** Spring Security, OAuth2, JWT
- **Documentation:** OpenAPI/Swagger

---

## Project Structure

```
nexus-spring-boot/
├── nexus-common/              # Shared common module
├── nexus-discovery-service/    # Service discovery
├── nexus-config-service/       # Configuration server
├── nexus-auth-service/         # OAuth2 server
├── nexus-api-gateway/          # API gateway
├── nexus-user-service/         # User management
├── nexus-product-service/      # Product catalog
├── nexus-order-service/        # Order processing
├── nexus-payment-service/      # Payment processing
├── nexus-cart-service/         # Shopping cart
├── nexus-marketplace-service/  # Marketplace
├── nexus-content-service/      # Content management
├── nexus-notification-service/ # Notifications
├── nexus-analytics-service/    # Analytics
└── nexus-admin-service/        # Admin dashboard
```

---

## Configuration

All services are configured to use local services:

- **PostgreSQL:** `localhost:5432`
- **Database:** `nexus_db`
- **Redis:** `localhost:6379`
- **Kafka:** `localhost:9092`

---

## Documentation

- **PROJECT_RENAME_COMPLETE.md** - Rename documentation
- **LOCAL_SETUP_GUIDE.md** - Local setup instructions (if exists)
- **API Documentation:** http://localhost:8080/swagger-ui.html

---

## Author

**code.papu@gmail.com**

---

## License

Proprietary - All rights reserved

---

**Nexus E-commerce Platform** - Modern, scalable, microservices-based e-commerce solution.
