#!/bin/bash

# Database Verification Script
# This script verifies that the database setup is complete and all tables exist

set -e

echo "=========================================="
echo "Vendo Database Verification"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Database connection details
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-vendo_db}"
DB_USER="${DB_USER:-vendo_app}"
DB_PASSWORD="${DB_PASSWORD:-REDACTED_DB_PASSWORD}"

# Check connection
echo "Testing database connection..."
if PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -c "SELECT 1;" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Database connection successful${NC}"
else
    echo -e "${RED}❌ Cannot connect to database${NC}"
    echo "   Please ensure PostgreSQL is running and credentials are correct"
    exit 1
fi

echo ""

# Check database exists
echo "Checking database..."
DB_EXISTS=$(PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'" 2>/dev/null || echo "0")
if [ "$DB_EXISTS" = "1" ]; then
    echo -e "${GREEN}✅ Database '${DB_NAME}' exists${NC}"
else
    echo -e "${RED}❌ Database '${DB_NAME}' does not exist${NC}"
    exit 1
fi

# Count tables
echo ""
echo "Counting tables..."
TABLE_COUNT=$(PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -tAc "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE';" 2>/dev/null || echo "0")
echo "Total tables: ${TABLE_COUNT}"

if [ "$TABLE_COUNT" -gt "0" ]; then
    echo -e "${GREEN}✅ Tables found in database${NC}"
else
    echo -e "${YELLOW}⚠️  No tables found. Run Flyway migrations to create tables.${NC}"
fi

# Check expected tables (from Flyway migrations)
echo ""
echo "Checking expected tables (from scripts/expected-tables.txt)..."
EXPECTED_FILE="$(dirname "$0")/expected-tables.txt"
FOUND=0
MISSING_KEY=0
if [ -f "$EXPECTED_FILE" ]; then
    while IFS= read -r line || [ -n "$line" ]; do
        line=$(echo "$line" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
        [ -z "$line" ] && continue
        [ "${line#\#}" != "$line" ] && continue
        table="$line"
        EXISTS=$(PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -tAc "SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = '${table}';" 2>/dev/null || echo "0")
        if [ "$EXISTS" = "1" ]; then
            ((FOUND++)) || true
        else
            echo -e "  ${YELLOW}⚠️${NC}  ${table} (not found)"
            ((MISSING_KEY++)) || true
        fi
    done < "$EXPECTED_FILE"
    echo "  Expected tables present: ${FOUND}"
    if [ "$MISSING_KEY" -gt 0 ]; then
        echo -e "  ${YELLOW}Missing: ${MISSING_KEY} tables. Run Flyway migrations. For full check use: ./scripts/verify-schema.sh${NC}"
    fi
else
    echo "  (expected-tables.txt not found; listing key tables only)"
    for table in users ec_products ec_orders carts categories retailers business_accounts inventory payments addresses; do
        EXISTS=$(PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -tAc "SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = '${table}';" 2>/dev/null || echo "0")
        if [ "$EXISTS" = "1" ]; then
            echo -e "  ${GREEN}✅${NC} ${table}"
        else
            echo -e "  ${YELLOW}⚠️${NC}  ${table} (not found)"
        fi
    done
fi

# Check Flyway schema history
echo ""
echo "Checking Flyway migration status..."
FLYWAY_EXISTS=$(PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -tAc "SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'flyway_schema_history';" 2>/dev/null || echo "0")
if [ "$FLYWAY_EXISTS" = "1" ]; then
    MIGRATION_COUNT=$(PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -tAc "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true;" 2>/dev/null || echo "0")
    echo -e "${GREEN}✅ Flyway schema history found${NC}"
    echo "  Successful migrations: ${MIGRATION_COUNT}"
else
    echo -e "${YELLOW}⚠️  Flyway schema history not found${NC}"
    echo "  Migrations have not been run yet"
fi

echo ""
echo "=========================================="
echo "Verification Complete"
echo "=========================================="
echo ""
echo "Database: ${DB_NAME}"
echo "Host: ${DB_HOST}:${DB_PORT}"
echo "User: ${DB_USER}"
echo "Tables: ${TABLE_COUNT}"
echo ""
echo "For strict schema verification (all expected tables, exit 1 if any missing):"
echo "  ./scripts/verify-schema.sh"
echo "  ./scripts/verify-schema.sh --columns   # also check key columns"
echo ""

