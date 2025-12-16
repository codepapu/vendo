#!/bin/bash

# Stop Laravel Services Script
# This script stops all Laravel-related services

set -e

echo "=========================================="
echo "Stopping Laravel Services"
echo "=========================================="

# Check if Laravel is running via Docker (optional - Docker not required)
if command -v docker &> /dev/null && docker ps | grep -q laravel; then
    echo "Stopping Laravel Docker containers (if any)..."
    docker stop $(docker ps -q --filter "name=laravel") 2>/dev/null || true
    echo "✅ Laravel containers stopped"
fi

# Check if Laravel is running via PHP built-in server
if pgrep -f "php.*artisan.*serve" > /dev/null; then
    echo "Stopping Laravel PHP built-in server..."
    pkill -f "php.*artisan.*serve" || true
    echo "✅ Laravel PHP server stopped"
fi

# Check if Laravel queue workers are running
if pgrep -f "php.*artisan.*queue:work" > /dev/null; then
    echo "Stopping Laravel queue workers..."
    pkill -f "php.*artisan.*queue:work" || true
    echo "✅ Laravel queue workers stopped"
fi

# Check if Laravel scheduler is running
if pgrep -f "php.*artisan.*schedule:run" > /dev/null; then
    echo "Stopping Laravel scheduler..."
    pkill -f "php.*artisan.*schedule:run" || true
    echo "✅ Laravel scheduler stopped"
fi

# Check for Apache/Nginx serving Laravel
if pgrep -f "apache2|httpd" > /dev/null; then
    echo "⚠️  Apache is running. You may need to stop it manually if it's serving Laravel."
    echo "   Command: sudo systemctl stop apache2 (or httpd)"
fi

if pgrep -f "nginx" > /dev/null; then
    echo "⚠️  Nginx is running. You may need to stop it manually if it's serving Laravel."
    echo "   Command: sudo systemctl stop nginx"
fi

echo ""
echo "=========================================="
echo "Laravel services stopped!"
echo "=========================================="
echo ""
echo "Note: If Laravel is running on a different port, you may need to:"
echo "  1. Stop the web server (Apache/Nginx)"
echo "  2. Stop any Laravel processes manually"
echo "  3. Update your frontend API endpoints to point to Spring Boot"
echo ""
