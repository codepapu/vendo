#!/bin/bash

# Script to commit Phase 11.5: External Integrations to git
# This safely handles the duplicate vendo-frontend folder and commits all changes

set -e

cd "$(dirname "$0")"

echo "=========================================="
echo "Phase 11.5: External Integrations - Git Commit"
echo "=========================================="
echo ""

# Step 1: Handle duplicate vendo-frontend folder
if [ -d "vendo-frontend" ] && [ -d "vendo/vendo-frontend" ]; then
    echo "✓ Found duplicate vendo-frontend folder"
    echo "  Main folder: vendo/vendo-frontend/ (has all files)"
    echo "  Duplicate: vendo-frontend/ (has only 3 files)"
    echo ""
    
    # Verify main folder has all necessary files
    if [ -f "vendo/vendo-frontend/src/services/analytics-api.ts" ] && \
       [ -f "vendo/vendo-frontend/src/services/reporting-api.ts" ] && \
       [ -f "vendo/vendo-frontend/src/types/reporting.ts" ]; then
        echo "✓ Main folder has all files (analytics-api.ts, reporting-api.ts, reporting.ts)"
        echo "  Removing duplicate folder..."
        rm -rf vendo-frontend
        echo "✓ Duplicate folder removed safely"
    else
        echo "⚠ Warning: Some files missing in main folder. Check manually before removing duplicate."
        exit 1
    fi
    echo ""
fi

# Step 2: Initialize git if needed
if [ ! -d .git ]; then
    echo "Initializing git repository..."
    git init
    echo "✓ Git repository initialized"
    echo ""
fi

# Step 3: Stage all changes
echo "Staging all changes..."
git add -A
echo "✓ All changes staged"
echo ""

# Step 4: Show what will be committed
echo "=== Files to be committed ==="
git status --short | head -20
echo ""

# Step 5: Create commit
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

# Ask for confirmation
read -p "Commit these changes? (y/n) " -n 1 -r
echo ""
if [[ $REPLY =~ ^[Yy]$ ]]; then
    git commit -m "$COMMIT_MSG"
    echo ""
    echo "✓ Changes committed successfully!"
    echo ""
    echo "Commit hash: $(git rev-parse --short HEAD)"
    echo ""
    echo "To push to remote:"
    echo "  git remote add origin <your-repo-url>"
    echo "  git push -u origin main"
else
    echo "Commit cancelled. Changes are staged and ready."
    echo "Run: git commit -m 'your message'"
fi

echo ""
echo "=========================================="
echo "Done!"
echo "=========================================="

