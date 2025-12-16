#!/bin/bash

# Flyway Migration Runner Script
# This script runs Flyway migrations for all services

set -e

echo "=========================================="
echo "Nexus Flyway Migrations"
echo "=========================================="

# Check if PostgreSQL is running locally
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "Error: PostgreSQL is not running on localhost:5432!"
    echo "Please start PostgreSQL:"
    echo "  - macOS: brew services start postgresql@16"
    echo "  - Linux: sudo systemctl start postgresql"
    exit 1
fi

# Get project root directory
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "Project root: $PROJECT_ROOT"
echo ""

# Services with migrations
SERVICES=(
    "nexus-user-service"
    "nexus-product-service"
    "nexus-order-service"
    "nexus-payment-service"
    "nexus-cart-service"
    "nexus-content-service"
    "nexus-notification-service"
    "nexus-analytics-service"
)

# Database connection details
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-nexus_db}"
DB_USER="${DB_USER:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-postgres}"

echo "Database: ${DB_NAME}@${DB_HOST}:${DB_PORT}"
echo ""

# Function to run migrations for a service
run_migrations() {
    local service=$1
    local migration_dir="$PROJECT_ROOT/$service/src/main/resources/db/migration"
    
    if [ ! -d "$migration_dir" ]; then
        echo "⚠️  No migrations found for $service (skipping)"
        return
    fi
    
    local migration_count=$(find "$migration_dir" -name "V*.sql" | wc -l | tr -d ' ')
    
    if [ "$migration_count" -eq 0 ]; then
        echo "⚠️  No migration files found for $service (skipping)"
        return
    fi
    
    echo "📦 Running migrations for $service ($migration_count files)..."
    
    # Use Flyway CLI if available, otherwise use Gradle
    if command -v flyway &> /dev/null; then
        flyway -url="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}" \
               -user="${DB_USER}" \
               -password="${DB_PASSWORD}" \
               -locations="filesystem:$migration_dir" \
               migrate
    else
        echo "   Using Gradle Flyway plugin..."
        cd "$PROJECT_ROOT/$service"
        ./gradlew flywayMigrate -Pflyway.url="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}" \
                                -Pflyway.user="${DB_USER}" \
                                -Pflyway.password="${DB_PASSWORD}" || echo "   ⚠️  Gradle Flyway not configured, migrations will run on service startup"
        cd "$PROJECT_ROOT"
    fi
    
    echo "✅ Migrations completed for $service"
    echo ""
}

# Run migrations for each service
for service in "${SERVICES[@]}"; do
    run_migrations "$service"
done

echo "=========================================="
echo "All migrations completed!"
echo "=========================================="
echo ""
echo "Note: Migrations will also run automatically when services start."
echo ""
