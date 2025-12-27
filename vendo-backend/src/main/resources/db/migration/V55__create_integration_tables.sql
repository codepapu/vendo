-- ============================================
-- External Integration Tables Migration
-- Creates tables for Accounting, CRM, and ERP integrations
-- ============================================

-- ============================================
-- 1. Integration Configurations Table
-- ============================================
CREATE TABLE integration_configurations (
    id BIGSERIAL PRIMARY KEY,
    integration_type VARCHAR(50) NOT NULL, -- ACCOUNTING, CRM, ERP
    provider VARCHAR(50) NOT NULL, -- QUICKBOOKS, XERO, SAGE, SALESFORCE, HUBSPOT, SAP, ORACLE, MICROSOFT_DYNAMICS
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    is_enabled BOOLEAN DEFAULT false,
    
    -- Connection Configuration (encrypted JSON)
    connection_config JSONB NOT NULL, -- API keys, tokens, endpoints, etc.
    
    -- Sync Configuration
    sync_direction VARCHAR(20) DEFAULT 'BIDIRECTIONAL', -- INBOUND, OUTBOUND, BIDIRECTIONAL
    auto_sync_enabled BOOLEAN DEFAULT false,
    sync_frequency VARCHAR(50), -- HOURLY, DAILY, WEEKLY, MANUAL
    last_sync_at TIMESTAMP,
    next_sync_at TIMESTAMP,
    
    -- Webhook Configuration
    webhook_url VARCHAR(500),
    webhook_secret VARCHAR(255),
    webhook_enabled BOOLEAN DEFAULT false,
    
    -- OAuth Configuration (if applicable)
    oauth_client_id VARCHAR(255),
    oauth_client_secret VARCHAR(255),
    oauth_access_token TEXT,
    oauth_refresh_token TEXT,
    oauth_token_expires_at TIMESTAMP,
    
    -- Metadata
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(integration_type, provider, name)
);

-- ============================================
-- 2. Integration Sync Logs Table
-- ============================================
CREATE TABLE integration_sync_logs (
    id BIGSERIAL PRIMARY KEY,
    integration_config_id BIGINT REFERENCES integration_configurations(id) ON DELETE CASCADE,
    sync_type VARCHAR(50) NOT NULL, -- ORDER_SYNC, INVOICE_SYNC, PAYMENT_SYNC, CUSTOMER_SYNC, PRODUCT_SYNC, INVENTORY_SYNC
    sync_direction VARCHAR(20) NOT NULL, -- INBOUND, OUTBOUND
    status VARCHAR(50) NOT NULL, -- PENDING, IN_PROGRESS, SUCCESS, FAILED, PARTIAL
    records_processed INTEGER DEFAULT 0,
    records_successful INTEGER DEFAULT 0,
    records_failed INTEGER DEFAULT 0,
    error_message TEXT,
    error_details JSONB,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    duration_ms BIGINT
);

-- ============================================
-- 3. Integration Sync Records Table
-- ============================================
CREATE TABLE integration_sync_records (
    id BIGSERIAL PRIMARY KEY,
    sync_log_id BIGINT REFERENCES integration_sync_logs(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL, -- ORDER, INVOICE, PAYMENT, CUSTOMER, PRODUCT, INVENTORY
    entity_id BIGINT NOT NULL, -- ID of the entity in our system
    external_id VARCHAR(255), -- ID in the external system
    external_reference VARCHAR(255), -- Reference number in external system
    sync_direction VARCHAR(20) NOT NULL, -- INBOUND, OUTBOUND
    status VARCHAR(50) NOT NULL, -- PENDING, SYNCED, FAILED, CONFLICT
    error_message TEXT,
    error_details JSONB,
    synced_data JSONB, -- Full data that was synced
    synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(entity_type, entity_id, external_id, sync_log_id)
);

-- ============================================
-- 4. Accounting Integration Specific Tables
-- ============================================
CREATE TABLE accounting_invoices (
    id BIGSERIAL PRIMARY KEY,
    integration_config_id BIGINT REFERENCES integration_configurations(id) ON DELETE CASCADE,
    invoice_id BIGINT REFERENCES invoices(id) ON DELETE CASCADE,
    external_invoice_id VARCHAR(255) NOT NULL, -- Invoice ID in accounting system
    external_invoice_number VARCHAR(255),
    sync_status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, SYNCED, FAILED, CONFLICT
    sync_direction VARCHAR(20) NOT NULL, -- INBOUND, OUTBOUND
    last_synced_at TIMESTAMP,
    external_data JSONB, -- Full invoice data from accounting system
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(integration_config_id, external_invoice_id),
    UNIQUE(invoice_id, integration_config_id)
);

CREATE TABLE accounting_payments (
    id BIGSERIAL PRIMARY KEY,
    integration_config_id BIGINT REFERENCES integration_configurations(id) ON DELETE CASCADE,
    payment_id BIGINT REFERENCES payments(id) ON DELETE CASCADE,
    external_payment_id VARCHAR(255) NOT NULL,
    external_transaction_id VARCHAR(255),
    sync_status VARCHAR(50) DEFAULT 'PENDING',
    sync_direction VARCHAR(20) NOT NULL,
    last_synced_at TIMESTAMP,
    external_data JSONB,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(integration_config_id, external_payment_id),
    UNIQUE(payment_id, integration_config_id)
);

-- ============================================
-- 5. CRM Integration Specific Tables
-- ============================================
CREATE TABLE crm_contacts (
    id BIGSERIAL PRIMARY KEY,
    integration_config_id BIGINT REFERENCES integration_configurations(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    external_contact_id VARCHAR(255) NOT NULL,
    external_account_id VARCHAR(255), -- Account/Company ID in CRM
    sync_status VARCHAR(50) DEFAULT 'PENDING',
    sync_direction VARCHAR(20) NOT NULL,
    last_synced_at TIMESTAMP,
    external_data JSONB,
    lifecycle_stage VARCHAR(50), -- LEAD, CUSTOMER, OPPORTUNITY, etc.
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(integration_config_id, external_contact_id),
    UNIQUE(user_id, integration_config_id)
);

CREATE TABLE crm_interactions (
    id BIGSERIAL PRIMARY KEY,
    crm_contact_id BIGINT REFERENCES crm_contacts(id) ON DELETE CASCADE,
    interaction_type VARCHAR(50) NOT NULL, -- ORDER, INQUIRY, SUPPORT, EMAIL, CALL, MEETING
    interaction_date TIMESTAMP NOT NULL,
    description TEXT,
    external_interaction_id VARCHAR(255),
    external_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 6. ERP Integration Specific Tables
-- ============================================
CREATE TABLE erp_products (
    id BIGSERIAL PRIMARY KEY,
    integration_config_id BIGINT REFERENCES integration_configurations(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    external_product_id VARCHAR(255) NOT NULL,
    external_sku VARCHAR(255),
    sync_status VARCHAR(50) DEFAULT 'PENDING',
    sync_direction VARCHAR(20) NOT NULL,
    last_synced_at TIMESTAMP,
    external_data JSONB,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(integration_config_id, external_product_id),
    UNIQUE(product_id, integration_config_id)
);

CREATE TABLE erp_inventory (
    id BIGSERIAL PRIMARY KEY,
    integration_config_id BIGINT REFERENCES integration_configurations(id) ON DELETE CASCADE,
    inventory_id BIGINT REFERENCES inventory(id) ON DELETE CASCADE,
    external_inventory_id VARCHAR(255) NOT NULL,
    external_warehouse_id VARCHAR(255),
    sync_status VARCHAR(50) DEFAULT 'PENDING',
    sync_direction VARCHAR(20) NOT NULL,
    last_synced_at TIMESTAMP,
    external_quantity INTEGER,
    external_data JSONB,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(integration_config_id, external_inventory_id),
    UNIQUE(inventory_id, integration_config_id)
);

CREATE TABLE erp_orders (
    id BIGSERIAL PRIMARY KEY,
    integration_config_id BIGINT REFERENCES integration_configurations(id) ON DELETE CASCADE,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    external_order_id VARCHAR(255) NOT NULL,
    external_order_number VARCHAR(255),
    sync_status VARCHAR(50) DEFAULT 'PENDING',
    sync_direction VARCHAR(20) NOT NULL,
    last_synced_at TIMESTAMP,
    external_data JSONB,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(integration_config_id, external_order_id),
    UNIQUE(order_id, integration_config_id)
);

-- ============================================
-- Indexes for Performance Optimization
-- ============================================

-- Integration Configurations Indexes
CREATE INDEX idx_integration_configs_type ON integration_configurations(integration_type);
CREATE INDEX idx_integration_configs_provider ON integration_configurations(provider);
CREATE INDEX idx_integration_configs_active ON integration_configurations(is_active);
CREATE INDEX idx_integration_configs_enabled ON integration_configurations(is_enabled);
CREATE INDEX idx_integration_configs_next_sync ON integration_configurations(next_sync_at) WHERE next_sync_at IS NOT NULL;

-- Integration Sync Logs Indexes
CREATE INDEX idx_sync_logs_config_id ON integration_sync_logs(integration_config_id);
CREATE INDEX idx_sync_logs_status ON integration_sync_logs(status);
CREATE INDEX idx_sync_logs_type ON integration_sync_logs(sync_type);
CREATE INDEX idx_sync_logs_started_at ON integration_sync_logs(started_at);

-- Integration Sync Records Indexes
CREATE INDEX idx_sync_records_log_id ON integration_sync_records(sync_log_id);
CREATE INDEX idx_sync_records_entity ON integration_sync_records(entity_type, entity_id);
CREATE INDEX idx_sync_records_external_id ON integration_sync_records(external_id);
CREATE INDEX idx_sync_records_status ON integration_sync_records(status);

-- Accounting Integration Indexes
CREATE INDEX idx_accounting_invoices_config ON accounting_invoices(integration_config_id);
CREATE INDEX idx_accounting_invoices_invoice ON accounting_invoices(invoice_id);
CREATE INDEX idx_accounting_invoices_external ON accounting_invoices(external_invoice_id);
CREATE INDEX idx_accounting_invoices_sync_status ON accounting_invoices(sync_status);
CREATE INDEX idx_accounting_payments_config ON accounting_payments(integration_config_id);
CREATE INDEX idx_accounting_payments_payment ON accounting_payments(payment_id);
CREATE INDEX idx_accounting_payments_external ON accounting_payments(external_payment_id);
CREATE INDEX idx_accounting_payments_sync_status ON accounting_payments(sync_status);

-- CRM Integration Indexes
CREATE INDEX idx_crm_contacts_config ON crm_contacts(integration_config_id);
CREATE INDEX idx_crm_contacts_user ON crm_contacts(user_id);
CREATE INDEX idx_crm_contacts_external ON crm_contacts(external_contact_id);
CREATE INDEX idx_crm_contacts_sync_status ON crm_contacts(sync_status);
CREATE INDEX idx_crm_interactions_contact ON crm_interactions(crm_contact_id);
CREATE INDEX idx_crm_interactions_type ON crm_interactions(interaction_type);
CREATE INDEX idx_crm_interactions_date ON crm_interactions(interaction_date);

-- ERP Integration Indexes
CREATE INDEX idx_erp_products_config ON erp_products(integration_config_id);
CREATE INDEX idx_erp_products_product ON erp_products(product_id);
CREATE INDEX idx_erp_products_external ON erp_products(external_product_id);
CREATE INDEX idx_erp_products_sync_status ON erp_products(sync_status);
CREATE INDEX idx_erp_inventory_config ON erp_inventory(integration_config_id);
CREATE INDEX idx_erp_inventory_inventory ON erp_inventory(inventory_id);
CREATE INDEX idx_erp_inventory_external ON erp_inventory(external_inventory_id);
CREATE INDEX idx_erp_inventory_sync_status ON erp_inventory(sync_status);
CREATE INDEX idx_erp_orders_config ON erp_orders(integration_config_id);
CREATE INDEX idx_erp_orders_order ON erp_orders(order_id);
CREATE INDEX idx_erp_orders_external ON erp_orders(external_order_id);
CREATE INDEX idx_erp_orders_sync_status ON erp_orders(sync_status);

