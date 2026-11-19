#!/bin/bash

# Schema Verification Script
# Verifies that all expected tables (from Flyway migrations) exist in the database.
# Optionally checks that key columns exist. Exit 0 only if all expected tables exist.
#
# Usage: ./scripts/verify-schema.sh [--columns]
#   --columns  Also verify key columns for critical tables (optional).
#
# Environment: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD (same as application)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EXPECTED_TABLES_FILE="${SCRIPT_DIR}/expected-tables.txt"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-vendo_db}"
DB_USER="${DB_USER:-vendo_app}"
DB_PASSWORD="${DB_PASSWORD:?Set DB_PASSWORD}"

CHECK_COLUMNS=false
if [ "${1:-}" = "--columns" ]; then
    CHECK_COLUMNS=true
fi

run_psql() {
    PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -tAc "$1" 2>/dev/null || echo ""
}

echo "=========================================="
echo "Vendo Schema Verification"
echo "=========================================="
echo ""

if ! PGPASSWORD=${DB_PASSWORD} psql -h ${DB_HOST} -p ${DB_PORT} -U ${DB_USER} -d ${DB_NAME} -c "SELECT 1;" > /dev/null 2>&1; then
    echo -e "${RED}❌ Cannot connect to database${NC}"
    exit 1
fi

if [ ! -f "$EXPECTED_TABLES_FILE" ]; then
    echo -e "${RED}❌ Expected tables file not found: $EXPECTED_TABLES_FILE${NC}"
    exit 1
fi

MISSING=()
while IFS= read -r line || [ -n "$line" ]; do
    line=$(echo "$line" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
    [ -z "$line" ] && continue
    [ "${line#\#}" != "$line" ] && continue
    table="$line"
    EXISTS=$(run_psql "SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE' AND table_name = '$table';")
    if [ "$EXISTS" != "1" ]; then
        MISSING+=("$table")
    fi
done < "$EXPECTED_TABLES_FILE"

if [ ${#MISSING[@]} -eq 0 ]; then
    echo -e "${GREEN}✅ All expected tables exist${NC}"
    TABLE_COUNT=$(run_psql "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE';")
    echo "  Total tables in public schema: ${TABLE_COUNT}"
else
    echo -e "${RED}❌ Missing tables (${#MISSING[@]}):${NC}"
    for t in "${MISSING[@]}"; do
        echo "  - $t"
    done
    echo ""
    echo "Run Flyway migrations: start the Spring Boot application or run: mvn flyway:migrate"
    exit 1
fi

# Optional: check Flyway ran
FLYWAY=$(run_psql "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'flyway_schema_history';")
if [ "$FLYWAY" = "1" ]; then
    N=$(run_psql "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true;")
    echo -e "${GREEN}✅ Flyway migrations applied: ${N} successful${NC}"
else
    echo -e "${YELLOW}⚠️  flyway_schema_history not found${NC}"
fi

if [ "$CHECK_COLUMNS" = true ]; then
    echo ""
    echo "Column check (key tables): checking presence of expected columns..."
    # Key tables and at least one expected column each
    key_checks="users:id,email|ec_products:id,name|ec_orders:id,code|carts:id,user_id|cart_items:id,cart_id"
    col_ok=0
    col_fail=0
    IFS='|'
    for spec in $key_checks; do
        tbl="${spec%%:*}"
        cols="${spec#*:}"
        for col in ${cols//,/ }; do
            C=$(run_psql "SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = '$tbl' AND column_name = '$col';")
            if [ "$C" = "1" ]; then
                ((col_ok++)) || true
            else
                echo -e "  ${YELLOW}⚠️${NC} $tbl.$col missing"
                ((col_fail++)) || true
            fi
        done
    done
    if [ "$col_fail" -eq 0 ]; then
        echo -e "${GREEN}✅ Key columns present${NC}"
    fi
fi

echo ""
echo "=========================================="
echo "Schema verification complete"
echo "=========================================="
exit 0
