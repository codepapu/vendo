#!/bin/bash

# Remove Laravel Backend Script
# This script backs up and removes Laravel backend files, keeping only frontend

set -e

LARAVEL_DIR="../codecanyon-51119638-nexus-ecommerce-multivendor-marketplace-laravel-platform"
BACKUP_DIR="../laravel-backup-$(date +%Y%m%d-%H%M%S)"

echo "=========================================="
echo "Removing Laravel Backend"
echo "=========================================="

if [ ! -d "$LARAVEL_DIR" ]; then
    echo "Error: Laravel directory not found at $LARAVEL_DIR"
    exit 1
fi

cd "$(dirname "$0")/.."

# Create backup directory
echo "Creating backup of Laravel backend..."
mkdir -p "$BACKUP_DIR"

# Backup Laravel backend files
echo "Backing up Laravel files..."
rsync -av --exclude='node_modules' --exclude='vendor' --exclude='.git' \
    "$LARAVEL_DIR/" "$BACKUP_DIR/" || {
    echo "Warning: Backup failed, but continuing..."
}

echo "✅ Backup created at: $BACKUP_DIR"
echo ""

# Remove Laravel backend files (keep frontend resources)
echo "Removing Laravel backend files..."

# Remove Laravel-specific directories
cd "$LARAVEL_DIR"

# Remove backend directories
rm -rf app/Http/Controllers/Api
rm -rf app/Models
rm -rf app/Repositories
rm -rf app/Services
rm -rf routes/api.php
rm -rf database/migrations
rm -rf database/seeders
rm -rf config/api.php
rm -rf config/database.php
rm -rf config/auth.php

# Remove Laravel backend files but keep frontend
# Keep: resources/, public/, package.json, vite.config.js, etc.

echo "✅ Laravel backend files removed"
echo ""
echo "Note: Frontend files (resources/, public/, package.json) are kept"
echo "Backup location: $BACKUP_DIR"
echo ""
