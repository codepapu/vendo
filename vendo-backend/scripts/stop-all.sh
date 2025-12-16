#!/bin/bash

# Stop All Applications Script
# This script stops both Spring Boot backend and frontend

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_DIR="../codecanyon-51119638-nexus-ecommerce-multivendor-marketplace-laravel-platform"

echo "=========================================="
echo "Stopping Nexus E-commerce Platform"
echo "=========================================="
echo ""

cd "$PROJECT_ROOT"

# Step 1: Stop Frontend
echo "Step 1: Stopping frontend..."
if [ -d "$FRONTEND_DIR" ]; then
    # Stop Vite dev server
    pkill -f "vite" || true
    # Stop npm dev server
    pkill -f "npm.*dev" || true
    echo "✅ Frontend stopped"
else
    echo "⚠️  Frontend directory not found"
fi
echo ""

# Step 2: Stop Spring Boot Services
echo "Step 2: Stopping Spring Boot services..."
./scripts/stop-services.sh
echo ""

# Step 3: Optionally stop infrastructure
read -p "Do you want to stop local infrastructure services (PostgreSQL, Redis, Kafka)? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Step 3: Stopping local infrastructure services..."
    echo "⚠️  Please stop services manually:"
    echo "  - PostgreSQL: brew services stop postgresql@16 (macOS) or sudo systemctl stop postgresql (Linux)"
    echo "  - Redis: brew services stop redis (macOS) or sudo systemctl stop redis (Linux)"
    echo "  - Kafka: brew services stop kafka (macOS) or stop Kafka service (Linux)"
else
    echo "Step 3: Keeping local infrastructure services running"
fi

echo ""
echo "=========================================="
echo "All Applications Stopped!"
echo "=========================================="
