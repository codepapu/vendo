#!/bin/bash

# Push Vendo project to GitHub repository

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_ROOT"

echo "=========================================="
echo "Pushing to GitHub: codepapu/vendo"
echo "=========================================="
echo ""

# Initialize git if not already initialized
if [ ! -d ".git" ]; then
    echo "Initializing git repository..."
    git init
    echo "✅ Git repository initialized"
    echo ""
fi

# Add all files
echo "Adding all files..."
git add -A

# Check if there are changes to commit
if git diff --staged --quiet; then
    echo "No changes to commit."
else
    # Commit
    echo "Creating commit..."
    git commit -m "Initial commit: Vendo e-commerce platform

- Monolithic Spring Boot 3.4.0 backend
- React 18 frontend with Vite
- Complete e-commerce features
- User management, products, orders, payments
- Modern UI/UX"
    echo "✅ Commit created"
    echo ""
fi

# Set branch to main
echo "Setting branch to main..."
git branch -M main

# Add remote (or update if exists)
echo "Configuring remote repository..."
git remote remove origin 2>/dev/null || true
git remote add origin https://github.com/codepapu/vendo.git
echo "✅ Remote configured: https://github.com/codepapu/vendo.git"
echo ""

# Push to GitHub
echo "Pushing to GitHub..."
echo "Note: You may be prompted for GitHub credentials"
echo ""
git push -u origin main

echo ""
echo "=========================================="
echo "✅ Successfully pushed to GitHub!"
echo "=========================================="
echo ""
echo "Repository: https://github.com/codepapu/vendo"
echo ""
