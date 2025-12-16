#!/bin/bash

# Start All Applications Script
# This script starts both Spring Boot backend and frontend

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_DIR="../codecanyon-51119638-nexus-ecommerce-multivendor-marketplace-laravel-platform"

echo "=========================================="
echo "Starting Nexus E-commerce Platform"
echo "=========================================="
echo ""

cd "$PROJECT_ROOT"

# Step 1: Check Local Infrastructure Services
echo "Step 1: Checking local infrastructure services..."

# Check PostgreSQL
if pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "✅ PostgreSQL is running"
else
    echo "❌ PostgreSQL is not running!"
    echo "Please start PostgreSQL:"
    echo "  - macOS: brew services start postgresql@16"
    echo "  - Linux: sudo systemctl start postgresql"
    exit 1
fi

# Check Redis
if redis-cli -h localhost -p 6379 ping > /dev/null 2>&1; then
    echo "✅ Redis is running"
else
    echo "⚠️  Redis is not running (optional but recommended)"
    echo "Start with: brew services start redis (macOS) or sudo systemctl start redis (Linux)"
fi

# Check Kafka
if lsof -i :9092 > /dev/null 2>&1; then
    echo "✅ Kafka is running"
else
    echo "⚠️  Kafka is not running (optional but recommended)"
    echo "Start with: brew services start kafka (macOS) or start Kafka service (Linux)"
fi

echo ""

# Step 2: Initialize databases (if not already done)
if ! PGPASSWORD=postgres psql -h localhost -p 5432 -U postgres -d nexus_db -c "SELECT 1;" &>/dev/null 2>&1; then
    echo "Step 2: Initializing databases..."
    ./scripts/init-databases.sh
    echo "✅ Databases initialized"
else
    echo "Step 2: Databases already initialized (skipping)"
fi
echo ""

# Step 3: Start Spring Boot Services
echo "Step 3: Starting Spring Boot services..."
./scripts/start-services.sh
echo ""

# Wait for backend services to start
echo "Waiting for backend services to start (30 seconds)..."
sleep 30

# Step 4: Start Frontend
if [ -d "$FRONTEND_DIR" ]; then
    echo "Step 4: Starting frontend application..."
    cd "$FRONTEND_DIR"
    
    # Check if node_modules exists
    if [ ! -d "node_modules" ]; then
        echo "Installing frontend dependencies..."
        npm install
    fi
    
    # Start frontend (Vite or Laravel Mix)
    if [ -f "vite.config.js" ] || [ -f "vite.config.ts" ]; then
        echo "Starting Vite dev server..."
        npm run dev &
        FRONTEND_PID=$!
        echo "✅ Frontend started (PID: $FRONTEND_PID)"
    elif [ -f "package.json" ]; then
        # Check for dev script
        if grep -q "\"dev\"" package.json; then
            echo "Starting frontend dev server..."
            npm run dev &
            FRONTEND_PID=$!
            echo "✅ Frontend started (PID: $FRONTEND_PID)"
        else
            echo "⚠️  No dev script found in package.json"
            echo "   Start frontend manually: cd $FRONTEND_DIR && npm run dev"
        fi
    else
        echo "⚠️  Frontend directory found but package.json not found"
    fi
    
    cd "$PROJECT_ROOT"
else
    echo "Step 4: Frontend directory not found at $FRONTEND_DIR"
    echo "   Start frontend manually"
fi

echo ""
echo "=========================================="
echo "All Applications Started!"
echo "=========================================="
echo ""
echo "Backend Services:"
echo "  - API Gateway: http://localhost:8080"
echo "  - Discovery Service: http://localhost:8761"
echo "  - Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""
echo "Frontend:"
echo "  - Check frontend URL (usually http://localhost:5173 for Vite)"
echo ""
echo "To stop all services: ./scripts/stop-all.sh"
echo ""
