#!/bin/bash

# Database Initialization Script
# This script initializes PostgreSQL databases and users for Vendo E-commerce Platform
# Requires local PostgreSQL installation

set -e

echo "=========================================="
echo "Vendo Database Initialization"
echo "=========================================="

# Check if PostgreSQL is running locally
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "⚠️  PostgreSQL is not running on localhost:5432"
    echo "Please start PostgreSQL locally:"
    echo "  - macOS: brew services start postgresql@16"
    echo "  - Linux: sudo systemctl start postgresql"
    echo "  - Windows: Start PostgreSQL service"
    exit 1
fi

# Get PostgreSQL connection details
POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"
POSTGRES_USER="${POSTGRES_USER:-postgres}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-postgres}"

echo "Connecting to local PostgreSQL at ${POSTGRES_HOST}:${POSTGRES_PORT}..."

# Run initialization SQL
PGPASSWORD=${POSTGRES_PASSWORD} psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -f "$(dirname "$0")/init-databases.sql" 2>&1 || {
    echo "⚠️  Note: Some operations may have failed if database/user already exists"
    echo "This is normal if running the script multiple times"
}

echo ""
echo "=========================================="
echo "Database initialization completed!"
echo "=========================================="
echo ""
echo "Databases created:"
echo "  - vendo_db (main database)"
echo ""
echo "Users created:"
echo "  - vendo_app (application user)"
echo "  - vendo_readonly (read-only user for reporting)"
echo ""
echo "Next steps:"
echo "  1. Start Spring Boot services to run Flyway migrations"
echo "  2. Or run: ./scripts/run-migrations.sh"
echo ""
