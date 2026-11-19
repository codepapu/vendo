#!/bin/bash
: "${DB_PASSWORD:?Set DB_PASSWORD to the vendo_app database password}"
: "${DB_READONLY_PASSWORD:?Set DB_READONLY_PASSWORD to the vendo_readonly password}"

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

# Auto-detect PostgreSQL superuser
# On macOS with Homebrew, it's usually the current user
# On Linux, it's usually 'postgres'
if [ -z "$POSTGRES_USER" ]; then
    # Try to detect the current user first (common on macOS/Homebrew)
    CURRENT_USER=$(whoami)
    if psql -U "$CURRENT_USER" -d postgres -c "SELECT 1;" > /dev/null 2>&1; then
        POSTGRES_USER="$CURRENT_USER"
    elif psql -U postgres -d postgres -c "SELECT 1;" > /dev/null 2>&1; then
        POSTGRES_USER="postgres"
    else
        # Last resort: try current user without password
        POSTGRES_USER="$CURRENT_USER"
    fi
fi

# Password is usually not needed for local connections (peer authentication)
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-}"

echo "Connecting to local PostgreSQL at ${POSTGRES_HOST}:${POSTGRES_PORT} as ${POSTGRES_USER}..."

# Run initialization SQL (creates database and users)
echo "Creating database and users..."
# Connect to 'postgres' database first (it always exists) or use template1
if [ -n "$POSTGRES_PASSWORD" ]; then
    PGPASSWORD=${POSTGRES_PASSWORD} psql -v app_password="$DB_PASSWORD" -v readonly_password="$DB_READONLY_PASSWORD" -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d postgres -f "$(dirname "$0")/init-databases.sql" 2>&1 || {
        # Try template1 if postgres doesn't exist
        PGPASSWORD=${POSTGRES_PASSWORD} psql -v app_password="$DB_PASSWORD" -v readonly_password="$DB_READONLY_PASSWORD" -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d template1 -f "$(dirname "$0")/init-databases.sql" 2>&1 || {
            echo "⚠️  Note: Some operations may have failed if database/user already exists"
            echo "This is normal if running the script multiple times"
        }
    }
else
    # Try without -h first (Unix socket, better for peer auth)
    psql -v app_password="$DB_PASSWORD" -v readonly_password="$DB_READONLY_PASSWORD" -U ${POSTGRES_USER} -d postgres -f "$(dirname "$0")/init-databases.sql" 2>&1 || {
        # Fallback to TCP/IP with template1
        psql -v app_password="$DB_PASSWORD" -v readonly_password="$DB_READONLY_PASSWORD" -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d template1 -f "$(dirname "$0")/init-databases.sql" 2>&1 || {
            echo "⚠️  Note: Some operations may have failed if database/user already exists"
            echo "This is normal if running the script multiple times"
        }
    }
fi

# Grant schema privileges (must be run on vendo_db)
echo "Granting schema privileges..."
# Wait a moment for database to be fully created
sleep 1

if [ -n "$POSTGRES_PASSWORD" ]; then
    PGPASSWORD=${POSTGRES_PASSWORD} psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d vendo_db -f "$(dirname "$0")/init-databases-schema.sql" 2>&1 || {
        echo "⚠️  Note: Some operations may have failed if privileges already granted"
        echo "   Or database may not exist yet"
    }
else
    # Try Unix socket first
    psql -U ${POSTGRES_USER} -d vendo_db -f "$(dirname "$0")/init-databases-schema.sql" 2>&1 || {
        # Fallback to TCP/IP
        psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d vendo_db -f "$(dirname "$0")/init-databases-schema.sql" 2>&1 || {
            echo "⚠️  Note: Some operations may have failed if privileges already granted"
            echo "   Or database may not exist yet"
        }
    }
fi

echo ""
echo "=========================================="
echo "Database initialization completed!"
echo "=========================================="
echo ""
echo "✅ Database created:"
echo "   - vendo_db (main database)"
echo ""
echo "✅ Users created:"
echo "   - vendo_app (application user)"
echo "     Password: (value of DB_PASSWORD)"
echo "   - vendo_readonly (read-only user for reporting)"
echo "     Password: (value of DB_READONLY_PASSWORD)"
echo ""
echo "📋 Next steps:"
echo "   1. Update application.yml with database credentials"
echo "   2. Start Spring Boot application to run Flyway migrations"
echo "   3. All tables will be created automatically via Flyway"
echo ""
echo "💡 Database connection details:"
echo "   URL: jdbc:postgresql://localhost:5432/vendo_db"
echo "   Username: vendo_app"
echo "   Password: (value of DB_PASSWORD)"
echo ""
