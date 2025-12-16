#!/bin/bash

# Update Frontend API Configuration Script
# This script updates frontend API endpoints to use Spring Boot

set -e

FRONTEND_DIR="../codecanyon-51119638-nexus-ecommerce-multivendor-marketplace-laravel-platform"
API_BASE_URL="http://localhost:8080"

echo "=========================================="
echo "Updating Frontend API Configuration"
echo "=========================================="

if [ ! -d "$FRONTEND_DIR" ]; then
    echo "Error: Frontend directory not found at $FRONTEND_DIR"
    exit 1
fi

cd "$FRONTEND_DIR"

# Find and update .env file
if [ -f ".env" ]; then
    echo "Updating .env file..."
    
    # Update API URL
    if grep -q "APP_URL\|API_URL\|VITE_API" .env; then
        sed -i.bak "s|APP_URL=.*|APP_URL=$API_BASE_URL|g" .env
        sed -i.bak "s|API_URL=.*|API_URL=$API_BASE_URL|g" .env
        sed -i.bak "s|VITE_API_URL=.*|VITE_API_URL=$API_BASE_URL|g" .env
        echo "✅ Updated .env file"
    else
        echo "APP_URL=$API_BASE_URL" >> .env
        echo "API_URL=$API_BASE_URL" >> .env
        echo "VITE_API_URL=$API_BASE_URL" >> .env
        echo "✅ Added API URLs to .env file"
    fi
else
    echo "⚠️  .env file not found, creating one..."
    cat > .env << EOF
APP_URL=$API_BASE_URL
API_URL=$API_BASE_URL
VITE_API_URL=$API_BASE_URL
EOF
    echo "✅ Created .env file"
fi

# Find and update JavaScript/Vue API configuration files
echo ""
echo "Searching for API configuration files..."

# Find API configuration files
API_FILES=$(find resources -type f \( -name "*.js" -o -name "*.vue" -o -name "*.ts" \) \
    -exec grep -l "api\|axios\|fetch\|http" {} \; 2>/dev/null | head -20)

if [ -n "$API_FILES" ]; then
    echo "Found API configuration files:"
    echo "$API_FILES" | while read file; do
        echo "  - $file"
        
        # Update common API URL patterns
        if [ -f "$file" ]; then
            # Create backup
            cp "$file" "$file.bak"
            
            # Update API URLs
            sed -i.tmp "s|http://localhost:8000|$API_BASE_URL|g" "$file"
            sed -i.tmp "s|http://127.0.0.1:8000|$API_BASE_URL|g" "$file"
            sed -i.tmp "s|/api/|/api/v1/|g" "$file"
            sed -i.tmp "s|api/|api/v1/|g" "$file"
            
            # Remove temp file
            rm -f "$file.tmp"
        fi
    done
    echo "✅ Updated API configuration files"
else
    echo "⚠️  No API configuration files found automatically"
    echo "   You may need to update manually"
fi

# Update package.json scripts if needed
if [ -f "package.json" ]; then
    echo ""
    echo "Checking package.json..."
    # package.json will be updated manually if needed
fi

echo ""
echo "=========================================="
echo "Frontend API Configuration Updated!"
echo "=========================================="
echo ""
echo "API Base URL: $API_BASE_URL"
echo ""
echo "Next steps:"
echo "  1. Review updated files (backups created with .bak extension)"
echo "  2. Update any hardcoded API endpoints in your frontend code"
echo "  3. Update authentication to use JWT tokens"
echo "  4. Test API calls"
echo ""
