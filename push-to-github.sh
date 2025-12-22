#!/bin/bash

# Push Vendo project to GitHub

set -e

cd "$(dirname "$0")"

echo "=========================================="
echo "Pushing to GitHub: codepapu/vendo"
echo "=========================================="
echo ""

# Initialize git
if [ ! -d ".git" ]; then
    echo "Initializing git..."
    git init
fi

# Add all files
echo "Adding all files..."
git add -A

# Commit
echo "Creating commit..."
git commit -m "Initial commit: Vendo e-commerce platform

- Monolithic Spring Boot 3.4.0 backend
- React 18 frontend with Vite
- Complete e-commerce features
- User management, products, orders, payments
- Modern UI/UX" || echo "No changes to commit"

# Set branch
git branch -M main

# Configure remote
echo "Configuring remote..."
git remote remove origin 2>/dev/null || true
git remote add origin https://github.com/codepapu/vendo.git

# Push
echo ""
echo "Pushing to GitHub..."
echo "You may be prompted for GitHub credentials"
echo ""
git push -u origin main

echo ""
echo "✅ Successfully pushed to GitHub!"
echo "Repository: https://github.com/codepapu/vendo"
