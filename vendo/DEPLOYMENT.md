# Vendo Platform Deployment Guide

This guide provides comprehensive instructions for deploying the complete Vendo e-commerce platform (Backend, Frontend, Mobile) to production.

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Prerequisites](#prerequisites)
4. [Backend Deployment](#backend-deployment)
5. [Frontend Deployment](#frontend-deployment)
6. [Mobile App Deployment](#mobile-app-deployment)
7. [Infrastructure Setup](#infrastructure-setup)
8. [CI/CD Pipeline](#cicd-pipeline)
9. [Monitoring](#monitoring)
10. [Scaling](#scaling)

## Overview

Vendo is a multi-tenant e-commerce platform consisting of:
- **Backend API** (Spring Boot) - Core business logic
- **Frontend Web App** (React) - Customer-facing web interface
- **Mobile App** (React Native) - Mobile applications

## Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Frontend  │────▶│   Backend   │────▶│  PostgreSQL │
│   (React)   │     │ (Spring Boot)│     │  Database   │
└─────────────┘     └─────────────┘     └─────────────┘
                           │
                    ┌──────┴──────┐
                    │             │
              ┌─────▼─────┐  ┌───▼────┐
              │   Redis   │  │   S3   │
              │   Cache   │  │ Storage│
              └───────────┘  └────────┘
```

## Prerequisites

### Infrastructure Requirements

- **Compute**: 2+ CPU cores, 4GB+ RAM per service
- **Database**: PostgreSQL 14+ with 20GB+ storage
- **Cache**: Redis 6+ with 1GB+ memory
- **Storage**: S3-compatible storage (AWS S3, MinIO)
- **Load Balancer**: For high availability
- **CDN**: For static assets (optional)

### Required Services

- Domain name with SSL certificate
- Email service (SendGrid, AWS SES)
- SMS service (Twilio, AWS SNS) - optional
- Payment gateway accounts
- Monitoring service (optional)

## Backend Deployment

See [vendo-backend/DEPLOYMENT.md](../vendo-backend/DEPLOYMENT.md) for detailed backend deployment instructions.

### Quick Start

```bash
cd vendo-backend
mvn clean package -DskipTests
java -jar target/vendo-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

## Frontend Deployment

### Build for Production

```bash
cd vendo-frontend
npm install
npm run build
```

Output will be in `dist/` directory.

### Deployment Options

#### Option 1: Static Hosting (Netlify, Vercel)

1. **Netlify**:
   - Connect repository
   - Build command: `npm run build`
   - Publish directory: `dist`
   - Environment variables: `VITE_API_URL=https://api.yourdomain.com/api`

2. **Vercel**:
   - Connect repository
   - Framework: Vite
   - Build command: `npm run build`
   - Output directory: `dist`
   - Environment variables: `VITE_API_URL`

#### Option 2: Nginx

1. Install Nginx:
```bash
sudo apt-get install nginx
```

2. Copy built files:
```bash
sudo cp -r dist/* /var/www/vendo/
```

3. Configure Nginx (`/etc/nginx/sites-available/vendo`):

```nginx
server {
    listen 80;
    server_name yourdomain.com;
    
    root /var/www/vendo;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    # Enable gzip
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
}
```

4. Enable site:
```bash
sudo ln -s /etc/nginx/sites-available/vendo /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

#### Option 3: Docker

Create `Dockerfile`:

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

Build and run:

```bash
docker build -t vendo-frontend .
docker run -p 80:80 vendo-frontend
```

## Mobile App Deployment

### iOS Deployment

1. **Build for App Store**:
```bash
cd vendo-mobile
eas build --platform ios --profile production
```

2. **Submit to App Store**:
```bash
eas submit --platform ios
```

### Android Deployment

1. **Build for Play Store**:
```bash
cd vendo-mobile
eas build --platform android --profile production
```

2. **Submit to Play Store**:
```bash
eas submit --platform android
```

### Configuration

Update `app.json`:

```json
{
  "expo": {
    "extra": {
      "apiUrl": "https://api.yourdomain.com/api"
    }
  }
}
```

## Infrastructure Setup

### Recommended Cloud Setup

#### AWS

1. **EC2** for backend (or ECS/Elastic Beanstalk)
2. **RDS PostgreSQL** for database
3. **ElastiCache Redis** for caching
4. **S3** for file storage
5. **CloudFront** for CDN
6. **Application Load Balancer** for routing

#### Google Cloud

1. **Cloud Run** for backend
2. **Cloud SQL PostgreSQL** for database
3. **Memorystore Redis** for caching
4. **Cloud Storage** for files
5. **Cloud CDN** for static assets

### Docker Compose (Development/Staging)

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

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data

  backend:
    build: ./vendo-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/vendo
      SPRING_DATASOURCE_USERNAME: vendo_user
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_REDIS_HOST: redis
    depends_on:
      - postgres
      - redis

  frontend:
    build: ./vendo-frontend
    ports:
      - "80:80"
    environment:
      VITE_API_URL: http://backend:8080/api
    depends_on:
      - backend

volumes:
  postgres_data:
  redis_data:
```

## CI/CD Pipeline

### GitHub Actions Example

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  deploy-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Build
        run: cd vendo-backend && mvn clean package -DskipTests
      - name: Deploy
        run: |
          # Add deployment steps

  deploy-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout
      - name: Setup Node
        uses: actions/setup-node@v3
        with:
          node-version: '20'
      - name: Build
        run: cd vendo-frontend && npm install && npm run build
      - name: Deploy
        run: |
          # Add deployment steps
```

## Monitoring

### Application Monitoring

- **Backend**: Use Spring Boot Actuator endpoints
- **Frontend**: Use error tracking (Sentry, LogRocket)
- **Database**: Monitor query performance
- **Cache**: Monitor Redis memory usage

### Log Aggregation

- Use centralized logging (ELK, CloudWatch, Stackdriver)
- Configure log rotation
- Set up log alerts

### Metrics

- CPU and memory usage
- Request latency
- Error rates
- Database connection pool
- Cache hit rates

## Scaling

### Horizontal Scaling

1. **Backend**: Deploy multiple instances behind load balancer
2. **Database**: Use read replicas for read-heavy workloads
3. **Cache**: Use Redis cluster for high availability
4. **CDN**: Use CDN for static assets

### Vertical Scaling

- Increase CPU and memory for services
- Optimize database queries
- Enable connection pooling
- Use caching effectively

## Production Checklist

- [ ] All services deployed and tested
- [ ] SSL/TLS certificates configured
- [ ] Environment variables set
- [ ] Database backups configured
- [ ] Monitoring and alerting set up
- [ ] Load testing completed
- [ ] Security audit completed
- [ ] Documentation updated
- [ ] Team trained on deployment process
- [ ] Disaster recovery plan in place

## Support

For deployment issues:
1. Check service logs
2. Verify environment variables
3. Review configuration files
4. Contact development team

