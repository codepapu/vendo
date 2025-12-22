#!/bin/bash

# Start Vendo Application
# Simple script to start the monolithic Spring Boot application

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "=========================================="
echo "Starting Vendo Application"
echo "=========================================="
echo ""

# Check if PostgreSQL is running
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "⚠️  PostgreSQL is not running on localhost:5432"
    echo "Please start PostgreSQL:"
    echo "  - macOS: brew services start postgresql@16"
    echo "  - Linux: sudo systemctl start postgresql"
    exit 1
fi

# Check if database exists
if ! PGPASSWORD=postgres psql -h localhost -p 5432 -U postgres -d vendo_db -c "SELECT 1;" &>/dev/null 2>&1; then
    echo "⚠️  Database 'vendo_db' does not exist"
    echo "Please run: ./scripts/init-databases.sh"
    exit 1
fi

echo "✅ PostgreSQL is running"
echo "✅ Database 'vendo_db' exists"
echo ""
echo "Starting application..."
echo ""

# Start the application
./gradlew bootRun
