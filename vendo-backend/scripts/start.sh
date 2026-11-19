#!/bin/bash
: "${DB_PASSWORD:?Set DB_PASSWORD to the vendo_app database password}"

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

# Check if database exists (using vendo_app user)
if ! PGPASSWORD="$DB_PASSWORD" psql -h localhost -p 5432 -U vendo_app -d vendo_db -c "SELECT 1;" &>/dev/null 2>&1; then
    echo "⚠️  Database 'vendo_db' does not exist or cannot connect"
    echo "Please ensure:"
    echo "  1. Database 'vendo_db' is created"
    echo "  2. User 'vendo_app' exists with correct password"
    echo "  3. Run: ./scripts/setup-database.sh"
    exit 1
fi

echo "✅ PostgreSQL is running"
echo "✅ Database 'vendo_db' exists"
echo ""
echo "Starting application..."
echo ""

# Start the application
mvn spring-boot:run
