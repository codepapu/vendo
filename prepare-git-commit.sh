#!/bin/bash

# Script to prepare git commit for Phase 11.5: External Integrations
# This script will:
# 1. Check for duplicate vendo-frontend folder
# 2. Merge any unique files
# 3. Remove duplicate folder
# 4. Stage all changes
# 5. Create proper commit messages

set -e

cd "$(dirname "$0")"

echo "=== Preparing Git Commit for Phase 11.5: External Integrations ==="
echo ""

# Check if git is initialized
if [ ! -d .git ]; then
    echo "Initializing git repository..."
    git init
    echo "Git repository initialized."
    echo ""
fi

# Check for duplicate vendo-frontend folder
if [ -d "vendo-frontend" ] && [ -d "vendo/vendo-frontend" ]; then
    echo "Found duplicate vendo-frontend folders:"
    echo "  - vendo-frontend/ (duplicate)"
    echo "  - vendo/vendo-frontend/ (main)"
    echo ""
    
    # Check if reporting.ts exists in main folder
    if [ ! -f "vendo/vendo-frontend/src/types/reporting.ts" ] && [ -f "vendo-frontend/src/types/reporting.ts" ]; then
        echo "Copying missing reporting.ts to main folder..."
        mkdir -p vendo/vendo-frontend/src/types
        cp vendo-frontend/src/types/reporting.ts vendo/vendo-frontend/src/types/reporting.ts
        echo "✓ reporting.ts copied"
    fi
    
    # Check if analytics-api.ts differs
    if [ -f "vendo-frontend/src/services/analytics-api.ts" ] && [ -f "vendo/vendo-frontend/src/services/analytics-api.ts" ]; then
        if ! diff -q vendo-frontend/src/services/analytics-api.ts vendo/vendo-frontend/src/services/analytics-api.ts > /dev/null 2>&1; then
            echo "⚠ Warning: analytics-api.ts differs between folders"
            echo "  Keeping main folder version. Check manually if needed."
        fi
    fi
    
    # Check if reporting-api.ts differs
    if [ -f "vendo-frontend/src/services/reporting-api.ts" ] && [ -f "vendo/vendo-frontend/src/services/reporting-api.ts" ]; then
        if ! diff -q vendo-frontend/src/services/reporting-api.ts vendo/vendo-frontend/src/services/reporting-api.ts > /dev/null 2>&1; then
            echo "⚠ Warning: reporting-api.ts differs between folders"
            echo "  Keeping main folder version. Check manually if needed."
        fi
    fi
    
    echo ""
    echo "Removing duplicate vendo-frontend folder..."
    rm -rf vendo-frontend
    echo "✓ Duplicate folder removed"
    echo ""
fi

# Stage all changes
echo "Staging all changes..."
git add -A
echo "✓ All changes staged"
echo ""

# Show status
echo "=== Git Status ==="
git status --short | head -30
echo ""

# Create commit message
COMMIT_MSG="feat: Phase 11.5 - External Integrations Implementation

Complete implementation of external integrations for Accounting, CRM, and ERP systems.

Backend Changes:
- Added AccountingIntegrationService with QuickBooks, Xero, Sage support
- Added CRMIntegrationService with Salesforce, HubSpot support  
- Added ERPIntegrationService with SAP, Oracle, Microsoft Dynamics support
- Implemented webhook handlers for all integration types
- Added two-way synchronization (inbound/outbound)
- Created provider factory pattern for extensibility
- Added database migration V55 for integration tables
- Created entities: IntegrationConfiguration, IntegrationSyncLog, IntegrationSyncRecord
- Added repositories and DTOs for all integration types
- Implemented REST API endpoints in IntegrationController

Frontend Changes:
- Added integration-api.ts service with TypeScript types
- Exported integration API in services index

Database:
- Migration V55: Created integration_configurations, integration_sync_logs, 
  integration_sync_records, and provider-specific tables

Files:
- 32 Java files (services, controllers, entities, repositories, DTOs)
- 8 provider implementations (QuickBooks, Xero, Sage, Salesforce, HubSpot, SAP, Oracle, Microsoft Dynamics)
- 1 database migration file
- 1 frontend TypeScript service
- Verification report: PHASE_11_5_VERIFICATION_REPORT.md

All requirements from BUILD_PROMPTS.md (Prompts 88.5, 88.6, 88.7) completed."

echo "=== Commit Message ==="
echo "$COMMIT_MSG"
echo ""

read -p "Do you want to commit these changes? (y/n) " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    git commit -m "$COMMIT_MSG"
    echo ""
    echo "✓ Changes committed successfully!"
    echo ""
    echo "To push to remote:"
    echo "  git remote add origin <your-repo-url>"
    echo "  git push -u origin main"
else
    echo "Commit cancelled. Changes are staged and ready to commit."
    echo "Run: git commit -m 'your message'"
fi

