#!/bin/bash

# Start Spring Boot Services Script
# This script starts all Spring Boot microservices

set -e

echo "=========================================="
echo "Starting Nexus Spring Boot Services"
echo "=========================================="

# Get project root directory
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

# Check if local services are running
echo "Checking local infrastructure services..."

# Check PostgreSQL
if pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "✅ PostgreSQL is running on localhost:5432"
else
    echo "⚠️  PostgreSQL is not running on localhost:5432"
    echo "Please start PostgreSQL:"
    echo "  - macOS: brew services start postgresql@16"
    echo "  - Linux: sudo systemctl start postgresql"
fi

# Check Redis
if redis-cli -h localhost -p 6379 ping > /dev/null 2>&1; then
    echo "✅ Redis is running on localhost:6379"
else
    echo "⚠️  Redis is not running on localhost:6379"
    echo "Please start Redis:"
    echo "  - macOS: brew services start redis"
    echo "  - Linux: sudo systemctl start redis"
fi

# Check Kafka
if lsof -i :9092 > /dev/null 2>&1; then
    echo "✅ Kafka is running on localhost:9092"
else
    echo "⚠️  Kafka is not running on localhost:9092"
    echo "Please start Kafka:"
    echo "  - macOS: brew services start kafka"
    echo "  - Linux: Start Kafka service"
fi

echo ""
echo "Starting Spring Boot services..."
echo ""

# Services to start (in dependency order)
SERVICES=(
    "nexus-discovery-service"
    "nexus-config-service"
    "nexus-auth-service"
    "nexus-user-service"
    "nexus-product-service"
    "nexus-cart-service"
    "nexus-order-service"
    "nexus-payment-service"
    "nexus-content-service"
    "nexus-notification-service"
    "nexus-analytics-service"
    "nexus-admin-service"
    "nexus-marketplace-service"
    "nexus-api-gateway"
)

# Function to start a service
start_service() {
    local service=$1
    local service_dir="$PROJECT_ROOT/$service"
    
    if [ ! -d "$service_dir" ]; then
        echo "⚠️  Service directory not found: $service (skipping)"
        return
    fi
    
    echo "🚀 Starting $service..."
    cd "$service_dir"
    
    # Check if Gradle wrapper exists
    if [ -f "./gradlew" ]; then
        # Start service in background
        nohup ./gradlew bootRun > "../logs/${service}.log" 2>&1 &
        echo "   Started in background (PID: $!)"
        echo "   Logs: logs/${service}.log"
    else
        echo "   ⚠️  Gradle wrapper not found, using system Gradle..."
        nohup gradle bootRun > "../logs/${service}.log" 2>&1 &
        echo "   Started in background (PID: $!)"
    fi
    
    cd "$PROJECT_ROOT"
    
    # Wait a bit before starting next service
    sleep 3
}

# Create logs directory
mkdir -p "$PROJECT_ROOT/logs"

# Start services
for service in "${SERVICES[@]}"; do
    start_service "$service"
done

echo ""
echo "=========================================="
echo "All services started!"
echo "=========================================="
echo ""
echo "Service URLs:"
echo "  - API Gateway: http://localhost:8080"
echo "  - Discovery Service: http://localhost:8761"
echo "  - Config Service: http://localhost:8888"
echo "  - User Service: http://localhost:8081"
echo "  - Product Service: http://localhost:8082"
echo "  - Order Service: http://localhost:8083"
echo "  - Payment Service: http://localhost:8084"
echo "  - Content Service: http://localhost:8085"
echo "  - Notification Service: http://localhost:8086"
echo "  - Analytics Service: http://localhost:8087"
echo "  - Admin Service: http://localhost:8088"
echo "  - Cart Service: http://localhost:8089"
echo "  - Marketplace Service: http://localhost:8090"
echo ""
echo "Check logs in: logs/"
echo ""
echo "To stop all services: ./scripts/stop-services.sh"
echo ""
