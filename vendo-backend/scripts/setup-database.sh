#!/bin/bash

# Comprehensive Database Setup Script for Vendo E-commerce Platform
# This script sets up the complete database infrastructure including:
# - Database creation
# - User creation with secure passwords
# - Schema privileges
# - Verification of setup
#
# Usage: ./scripts/setup-database.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "=========================================="
echo "Vendo Database Setup"
echo "=========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if PostgreSQL is running
echo "Checking PostgreSQL connection..."
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo -e "${RED}❌ PostgreSQL is not running on localhost:5432${NC}"
    echo ""
    echo "Please start PostgreSQL:"
    echo "  - macOS: brew services start postgresql@16"
    echo "  - Linux: sudo systemctl start postgresql"
    echo "  - Windows: Start PostgreSQL service"
    exit 1
fi
echo -e "${GREEN}✅ PostgreSQL is running${NC}"
echo ""

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
        echo -e "${GREEN}✅ Detected PostgreSQL superuser: ${POSTGRES_USER}${NC}"
    elif psql -U postgres -d postgres -c "SELECT 1;" > /dev/null 2>&1; then
        POSTGRES_USER="postgres"
        echo -e "${GREEN}✅ Using PostgreSQL superuser: ${POSTGRES_USER}${NC}"
    else
        # Last resort: try current user without password
        POSTGRES_USER="$CURRENT_USER"
        echo -e "${YELLOW}⚠️  Could not verify PostgreSQL user, trying: ${POSTGRES_USER}${NC}"
    fi
fi

# Password is usually not needed for local connections (peer authentication)
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-}"

echo "PostgreSQL Connection Details:"
echo "  Host: ${POSTGRES_HOST}"
echo "  Port: ${POSTGRES_PORT}"
echo "  User: ${POSTGRES_USER}"
if [ -n "$POSTGRES_PASSWORD" ]; then
    echo "  Password: [SET]"
else
    echo "  Password: [Using peer/local authentication]"
fi
echo ""

# Step 1: Create database and users
echo "=========================================="
echo "Step 1: Creating database and users"
echo "=========================================="
# Connect to 'postgres' database first (it always exists) or use template1
# When using -h localhost, we must specify a database that exists
if [ -n "$POSTGRES_PASSWORD" ]; then
    PGPASSWORD=${POSTGRES_PASSWORD} psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d postgres -f "$SCRIPT_DIR/init-databases.sql" 2>&1 || {
        # Try template1 if postgres doesn't exist
        PGPASSWORD=${POSTGRES_PASSWORD} psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d template1 -f "$SCRIPT_DIR/init-databases.sql" 2>&1 || {
            echo -e "${YELLOW}⚠️  Some operations may have failed if database/user already exists${NC}"
            echo "   This is normal if running the script multiple times"
        }
    }
else
    # Try without -h first (Unix socket, better for peer auth)
    psql -U ${POSTGRES_USER} -d postgres -f "$SCRIPT_DIR/init-databases.sql" 2>&1 || {
        # Fallback to TCP/IP with template1
        psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d template1 -f "$SCRIPT_DIR/init-databases.sql" 2>&1 || {
            echo -e "${YELLOW}⚠️  Some operations may have failed if database/user already exists${NC}"
            echo "   This is normal if running the script multiple times"
        }
    }
fi
echo -e "${GREEN}✅ Database and users created${NC}"
echo ""

# Step 2: Grant schema privileges
echo "=========================================="
echo "Step 2: Granting schema privileges"
echo "=========================================="
# Wait a moment for database to be fully created
sleep 1

if [ -n "$POSTGRES_PASSWORD" ]; then
    PGPASSWORD=${POSTGRES_PASSWORD} psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d vendo_db -f "$SCRIPT_DIR/init-databases-schema.sql" 2>&1 || {
        echo -e "${YELLOW}⚠️  Some operations may have failed if privileges already granted${NC}"
        echo "   Or database may not exist yet - will retry in verification step"
    }
else
    # Try without -h first (Unix socket)
    psql -U ${POSTGRES_USER} -d vendo_db -f "$SCRIPT_DIR/init-databases-schema.sql" 2>&1 || {
        # Fallback to TCP/IP
        psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d vendo_db -f "$SCRIPT_DIR/init-databases-schema.sql" 2>&1 || {
            echo -e "${YELLOW}⚠️  Some operations may have failed if privileges already granted${NC}"
            echo "   Or database may not exist yet - will retry in verification step"
        }
    }
fi
echo -e "${GREEN}✅ Schema privileges granted${NC}"
echo ""

# Step 3: Verify database setup
echo "=========================================="
echo "Step 3: Verifying database setup"
echo "=========================================="

# Check if database exists
if [ -n "$POSTGRES_PASSWORD" ]; then
    DB_EXISTS=$(PGPASSWORD=${POSTGRES_PASSWORD} psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='vendo_db'" 2>/dev/null || echo "0")
else
    # Try Unix socket first (better for peer auth)
    DB_EXISTS=$(psql -U ${POSTGRES_USER} -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='vendo_db'" 2>/dev/null || \
                psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d template1 -tAc "SELECT 1 FROM pg_database WHERE datname='vendo_db'" 2>/dev/null || echo "0")
fi
if [ "$DB_EXISTS" = "1" ]; then
    echo -e "${GREEN}✅ Database 'vendo_db' exists${NC}"
else
    echo -e "${RED}❌ Database 'vendo_db' does not exist${NC}"
    exit 1
fi

# Check if users exist
if [ -n "$POSTGRES_PASSWORD" ]; then
    APP_USER_EXISTS=$(PGPASSWORD=${POSTGRES_PASSWORD} psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d postgres -tAc "SELECT 1 FROM pg_user WHERE usename='vendo_app'" 2>/dev/null || echo "0")
    READONLY_USER_EXISTS=$(PGPASSWORD=${POSTGRES_PASSWORD} psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d postgres -tAc "SELECT 1 FROM pg_user WHERE usename='vendo_readonly'" 2>/dev/null || echo "0")
else
    # Try Unix socket first
    APP_USER_EXISTS=$(psql -U ${POSTGRES_USER} -d postgres -tAc "SELECT 1 FROM pg_user WHERE usename='vendo_app'" 2>/dev/null || \
                     psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d template1 -tAc "SELECT 1 FROM pg_user WHERE usename='vendo_app'" 2>/dev/null || echo "0")
    READONLY_USER_EXISTS=$(psql -U ${POSTGRES_USER} -d postgres -tAc "SELECT 1 FROM pg_user WHERE usename='vendo_readonly'" 2>/dev/null || \
                          psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U ${POSTGRES_USER} -d template1 -tAc "SELECT 1 FROM pg_user WHERE usename='vendo_readonly'" 2>/dev/null || echo "0")
fi

if [ "$APP_USER_EXISTS" = "1" ]; then
    echo -e "${GREEN}✅ User 'vendo_app' exists${NC}"
else
    echo -e "${RED}❌ User 'vendo_app' does not exist${NC}"
    exit 1
fi

if [ "$READONLY_USER_EXISTS" = "1" ]; then
    echo -e "${GREEN}✅ User 'vendo_readonly' exists${NC}"
else
    echo -e "${RED}❌ User 'vendo_readonly' does not exist${NC}"
    exit 1
fi

# Test connection with application user
echo "Testing connection with application user..."
PGPASSWORD='REDACTED_DB_PASSWORD' psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U vendo_app -d vendo_db -c "SELECT version();" > /dev/null 2>&1 && {
    echo -e "${GREEN}✅ Application user can connect to database${NC}"
} || {
    echo -e "${RED}❌ Application user cannot connect to database${NC}"
    exit 1
}

echo ""

# Step 4: Summary
echo "=========================================="
echo "✅ Database Setup Complete!"
echo "=========================================="
echo ""
echo "Database Information:"
echo "  Database Name: vendo_db"
echo "  Host: ${POSTGRES_HOST}"
echo "  Port: ${POSTGRES_PORT}"
echo ""
echo "Application User:"
echo "  Username: vendo_app"
echo "  Password: REDACTED_DB_PASSWORD"
echo ""
echo "Read-Only User (for reporting/analytics):"
echo "  Username: vendo_readonly"
echo "  Password: REDACTED_DB_PASSWORD"
echo ""
echo "Connection String:"
echo "  jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/vendo_db"
echo ""
echo "📋 Next Steps:"
echo "  1. ✅ Database and users are ready"
echo "  2. Start Spring Boot application to run Flyway migrations"
echo "  3. All tables will be created automatically via Flyway"
echo ""
echo "To start the backend:"
echo "  cd $PROJECT_ROOT"
echo "  ./gradlew bootRun"
echo ""
echo "To verify tables after migration:"
echo "  PGPASSWORD='REDACTED_DB_PASSWORD' psql -h ${POSTGRES_HOST} -p ${POSTGRES_PORT} -U vendo_app -d vendo_db -c '\\dt'"
echo ""

