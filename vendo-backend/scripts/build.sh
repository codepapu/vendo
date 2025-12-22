#!/bin/bash

# Build Vendo Application
# Simple script to build the monolithic Spring Boot application

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "=========================================="
echo "Building Vendo Application"
echo "=========================================="
echo ""

./gradlew clean build

echo ""
echo "✅ Build complete!"
echo ""
