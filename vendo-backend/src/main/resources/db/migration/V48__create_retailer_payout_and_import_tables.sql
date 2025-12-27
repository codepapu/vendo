-- Migration: V48__create_retailer_payout_and_import_tables.sql
-- Description: Create tables for retailer payouts and product import history
-- Date: Created for Retailer Payout and Bulk Import features

-- ============================================
-- 1. Retailer Payouts Table
-- ============================================
CREATE TABLE retailer_payouts (
    id BIGSERIAL PRIMARY KEY,
    retailer_id BIGINT REFERENCES retailers(id) ON DELETE CASCADE,
    payout_amount DECIMAL(15,2) NOT NULL,
    total_sales DECIMAL(15,2),
    total_commission DECIMAL(15,2),
    period_start TIMESTAMP NOT NULL,
    period_end TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    payout_method VARCHAR(50), -- BANK_TRANSFER, PAYPAL, STRIPE
    transaction_id VARCHAR(255),
    bank_account_id BIGINT REFERENCES retailer_bank_accounts(id),
    notes TEXT,
    processed_at TIMESTAMP,
    processed_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for retailer_payouts
CREATE INDEX idx_retailer_payouts_retailer_id ON retailer_payouts(retailer_id);
CREATE INDEX idx_retailer_payouts_status ON retailer_payouts(status);
CREATE INDEX idx_retailer_payouts_period ON retailer_payouts(period_start, period_end);

-- ============================================
-- 2. Product Import History Table
-- ============================================
CREATE TABLE product_import_history (
    id BIGSERIAL PRIMARY KEY,
    retailer_id BIGINT REFERENCES retailers(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50), -- CSV, XLSX, XLS
    total_records INTEGER,
    success_count INTEGER,
    error_count INTEGER,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, PROCESSING, COMPLETED, FAILED
    error_details TEXT, -- JSON string of errors
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for product_import_history
CREATE INDEX idx_product_import_history_retailer_id ON product_import_history(retailer_id);
CREATE INDEX idx_product_import_history_status ON product_import_history(status);
CREATE INDEX idx_product_import_history_created_at ON product_import_history(created_at);

