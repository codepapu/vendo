# Vendo E-commerce Platform

A modern, full-stack e-commerce platform built with Spring Boot and React.

## Architecture

- **Backend**: Monolithic Spring Boot 3.4.0 application
- **Frontend**: React 18 with Vite
- **Database**: PostgreSQL
- **Cache**: Redis (optional)
- **Message Broker**: Kafka (optional)

## Project Structure

```
vendo/
├── vendo-backend/      # Spring Boot monolithic application
│   ├── src/
│   │   ├── main/java/com/vendo/  # All services consolidated
│   │   └── main/resources/       # Configuration and migrations
│   └── build.gradle.kts
│
└── vendo-frontend/     # React frontend application
    ├── src/            # React components and pages
    ├── public/         # Static assets
    └── package.json
```

## Quick Start

### Backend

```bash
cd vendo-backend

# Initialize database
./scripts/init-databases.sh

# Build and run
./gradlew bootRun
```

Backend will run on `http://localhost:8080`

### Frontend

```bash
cd vendo-frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

Frontend will run on `http://localhost:3000`

## Features

### Backend
- ✅ User Management & Authentication (JWT, OAuth2)
- ✅ Product Catalog Management
- ✅ Shopping Cart & Checkout
- ✅ Order Processing
- ✅ Payment Processing (Multiple Gateways)
- ✅ Multi-vendor Marketplace
- ✅ Content Management
- ✅ Notifications (Email, SMS, Push)
- ✅ Analytics & Reporting
- ✅ Admin Dashboard

### Frontend
- ✅ Product Listing & Search
- ✅ Product Detail Pages
- ✅ Shopping Cart
- ✅ Checkout Flow
- ✅ User Authentication
- ✅ Responsive Design

## Technology Stack

### Backend
- Spring Boot 3.4.0
- Java 21
- PostgreSQL
- Redis
- Kafka
- Flyway (Database Migrations)

### Frontend
- React 18
- Vite
- React Router
- TanStack Query
- Axios

## API Documentation

Once the backend is running, access Swagger UI at:
- http://localhost:8080/swagger-ui.html

## Development

### Backend Development
```bash
cd vendo-backend
./gradlew bootRun
```

### Frontend Development
```bash
cd vendo-frontend
npm run dev
```

## Build for Production

### Backend
```bash
cd vendo-backend
./gradlew clean build
./gradlew bootJar
```

### Frontend
```bash
cd vendo-frontend
npm run build
```

## Environment Variables

### Backend
See `vendo-backend/src/main/resources/application.yml`

### Frontend
Create `.env` file:
```
VITE_API_URL=http://localhost:8080/api
```

## License

Proprietary - All rights reserved

## Author

code.papu@gmail.com

---

**Vendo E-commerce Platform** - Modern, scalable, full-stack e-commerce solution.
