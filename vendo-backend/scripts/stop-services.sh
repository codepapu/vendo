#!/bin/bash

# Stop Spring Boot Services Script
# This script stops all Spring Boot microservices

set -e

echo "=========================================="
echo "Stopping Nexus Spring Boot Services"
echo "=========================================="

# Stop all Java processes running Spring Boot
echo "Stopping Spring Boot services..."
pkill -f "spring-boot:run" || true
pkill -f "bootRun" || true

# Wait a moment for graceful shutdown
sleep 2

# Force kill if still running
pkill -9 -f "spring-boot:run" 2>/dev/null || true
pkill -9 -f "bootRun" 2>/dev/null || true

echo "✅ All Spring Boot services stopped"
echo ""

# Optionally stop infrastructure services
read -p "Do you want to stop local infrastructure services (PostgreSQL, Redis, Kafka)? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Stopping local infrastructure services..."
    echo "⚠️  Please stop services manually:"
    echo "  - PostgreSQL: brew services stop postgresql@16 (macOS) or sudo systemctl stop postgresql (Linux)"
    echo "  - Redis: brew services stop redis (macOS) or sudo systemctl stop redis (Linux)"
    echo "  - Kafka: brew services stop kafka (macOS) or stop Kafka service (Linux)"
fi

echo ""
echo "=========================================="
echo "All services stopped!"
echo "=========================================="
