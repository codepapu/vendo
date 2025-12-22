#!/bin/bash

# Commit all changes to git

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_ROOT"

echo "=========================================="
echo "Committing to Git"
echo "=========================================="
echo ""

# Initialize git if not already initialized
if [ ! -d ".git" ]; then
    echo "Initializing git repository..."
    git init
    echo "✅ Git repository initialized"
    echo ""
fi

# Add .gitignore if it exists
if [ -f ".gitignore" ]; then
    echo "Adding .gitignore..."
    git add .gitignore
fi

# Add all files
echo "Adding all files to git..."
git add -A

# Show status
echo ""
echo "Files to be committed:"
git status --short | head -20
echo ""

# Commit
echo "Creating commit..."
git commit -m "Initial commit: Vendo e-commerce platform

- Monolithic Spring Boot 3.4.0 backend
- React 18 frontend with Vite
- Complete e-commerce features
- User management, products, orders, payments
- Modern UI/UX"

echo ""
echo "=========================================="
echo "✅ Successfully committed to git!"
echo "=========================================="
echo ""
echo "Commit created successfully."
echo ""
echo "To push to remote repository:"
echo "  git remote add origin <your-repo-url>"
echo "  git push -u origin main"
echo ""
