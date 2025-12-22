#!/bin/bash

# Delete all old Laravel files and folders from vendo-frontend

set -e

cd "$(dirname "$0")"

echo "=========================================="
echo "Deleting Old Files and Folders"
echo "=========================================="
echo ""

# Delete old Laravel directories
echo "Deleting old directories..."
rm -rf storage
rm -rf resources
rm -rf public/vendor
rm -rf public/storage
rm -rf public/themes
rm -rf public/docs
rm -f public/.htaccess
rm -f public/web.config
rm -f public/favicon.ico

# Clean public directory - keep only vite.svg
echo "Cleaning public directory..."
if [ -d "public" ]; then
    find public -type f ! -name "vite.svg" -delete 2>/dev/null || true
    find public -type d -empty -delete 2>/dev/null || true
fi

# Remove old node_modules and package-lock.json (will be reinstalled)
echo "Removing old dependencies..."
rm -rf node_modules
rm -f package-lock.json

echo ""
echo "=========================================="
echo "✅ Cleanup Complete!"
echo "=========================================="
echo ""
echo "Old files deleted. Current structure:"
echo "  - src/          (React application)"
echo "  - public/       (Only vite.svg)"
echo "  - package.json  (Dependencies)"
echo ""
echo "Next step: Run 'npm install' to install dependencies"
echo ""
