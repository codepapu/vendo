-- Migration: V2__create_b2b_tables.sql
-- Description: Create B2B functionality tables including business_accounts, business_contacts, 
--              b2b_pricing_tiers, purchase_orders, and invoices
-- Date: Created for B2B feature implementation

-- ============================================
-- 1. Business Accounts Table
-- ============================================
CREATE TABLE business_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(255) NOT NULL,
    business_registration_number VARCHAR(100),
    tax_id VARCHAR(100),
    business_type VARCHAR(50), -- CORPORATE, SME, ENTERPRISE
    credit_limit DECIMAL(15,2) DEFAULT 0,
    credit_terms_days INTEGER DEFAULT 30,
    payment_terms TEXT,
    billing_address_id BIGINT,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, SUSPENDED
    approved_at TIMESTAMP,
    approved_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 2. Business Contacts Table
-- ============================================
CREATE TABLE business_contacts (
    id BIGSERIAL PRIMARY KEY,
    business_account_id BIGINT REFERENCES business_accounts(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id),
    contact_type VARCHAR(50), -- PRIMARY, BILLING, SHIPPING, PURCHASER
    is_primary BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 3. B2B Pricing Tiers Table
-- ============================================
CREATE TABLE b2b_pricing_tiers (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    business_type VARCHAR(50), -- CORPORATE, SME, ENTERPRISE
    min_quantity INTEGER NOT NULL,
    max_quantity INTEGER,
    price DECIMAL(10,2) NOT NULL,
    discount_percentage DECIMAL(5,2),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 4. Purchase Orders Table
-- ============================================
CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    po_number VARCHAR(50) UNIQUE NOT NULL,
    business_account_id BIGINT REFERENCES business_accounts(id),
    order_id BIGINT REFERENCES orders(id),
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, COMPLETED
    requested_by BIGINT REFERENCES users(id),
    approved_by BIGINT REFERENCES users(id),
    approval_notes TEXT,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 5. Invoices Table (B2B)
-- ============================================
CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    order_id BIGINT REFERENCES orders(id),
    business_account_id BIGINT REFERENCES business_accounts(id),
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, PAID, OVERDUE, CANCELLED
    subtotal DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL,
    paid_amount DECIMAL(15,2) DEFAULT 0,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Indexes for Performance Optimization
-- ============================================

-- Business Accounts Indexes
CREATE INDEX idx_business_accounts_user_id ON business_accounts(user_id);
CREATE INDEX idx_business_accounts_status ON business_accounts(status);
CREATE INDEX idx_business_accounts_business_type ON business_accounts(business_type);
CREATE INDEX idx_business_accounts_approved_by ON business_accounts(approved_by);
CREATE INDEX idx_business_accounts_created_at ON business_accounts(created_at);

-- Business Contacts Indexes
CREATE INDEX idx_business_contacts_business_account_id ON business_contacts(business_account_id);
CREATE INDEX idx_business_contacts_user_id ON business_contacts(user_id);
CREATE INDEX idx_business_contacts_contact_type ON business_contacts(contact_type);
CREATE INDEX idx_business_contacts_is_primary ON business_contacts(is_primary);

-- B2B Pricing Tiers Indexes
CREATE INDEX idx_b2b_pricing_tiers_product_id ON b2b_pricing_tiers(product_id);
CREATE INDEX idx_b2b_pricing_tiers_business_type ON b2b_pricing_tiers(business_type);
CREATE INDEX idx_b2b_pricing_tiers_is_active ON b2b_pricing_tiers(is_active);
CREATE INDEX idx_b2b_pricing_tiers_product_business_type ON b2b_pricing_tiers(product_id, business_type);

-- Purchase Orders Indexes
CREATE INDEX idx_purchase_orders_po_number ON purchase_orders(po_number);
CREATE INDEX idx_purchase_orders_business_account_id ON purchase_orders(business_account_id);
CREATE INDEX idx_purchase_orders_order_id ON purchase_orders(order_id);
CREATE INDEX idx_purchase_orders_status ON purchase_orders(status);
CREATE INDEX idx_purchase_orders_requested_by ON purchase_orders(requested_by);
CREATE INDEX idx_purchase_orders_approved_by ON purchase_orders(approved_by);
CREATE INDEX idx_purchase_orders_created_at ON purchase_orders(created_at);

-- Invoices Indexes
CREATE INDEX idx_invoices_invoice_number ON invoices(invoice_number);
CREATE INDEX idx_invoices_order_id ON invoices(order_id);
CREATE INDEX idx_invoices_business_account_id ON invoices(business_account_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_issue_date ON invoices(issue_date);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_invoices_created_at ON invoices(created_at);

-- ============================================
-- Additional Constraints
-- ============================================

-- Ensure credit_limit is non-negative
ALTER TABLE business_accounts ADD CONSTRAINT chk_business_accounts_credit_limit 
    CHECK (credit_limit >= 0);

-- Ensure credit_terms_days is positive
ALTER TABLE business_accounts ADD CONSTRAINT chk_business_accounts_credit_terms_days 
    CHECK (credit_terms_days > 0);

-- Ensure min_quantity is positive for pricing tiers
ALTER TABLE b2b_pricing_tiers ADD CONSTRAINT chk_b2b_pricing_tiers_min_quantity 
    CHECK (min_quantity > 0);

-- Ensure max_quantity is greater than or equal to min_quantity if both are set
ALTER TABLE b2b_pricing_tiers ADD CONSTRAINT chk_b2b_pricing_tiers_quantity_range 
    CHECK (max_quantity IS NULL OR max_quantity >= min_quantity);

-- Ensure price is positive for pricing tiers
ALTER TABLE b2b_pricing_tiers ADD CONSTRAINT chk_b2b_pricing_tiers_price 
    CHECK (price > 0);

-- Ensure discount_percentage is between 0 and 100 if set
ALTER TABLE b2b_pricing_tiers ADD CONSTRAINT chk_b2b_pricing_tiers_discount_percentage 
    CHECK (discount_percentage IS NULL OR (discount_percentage >= 0 AND discount_percentage <= 100));

-- Ensure due_date is after or equal to issue_date for invoices
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_date_range 
    CHECK (due_date >= issue_date);

-- Ensure amounts are non-negative for invoices
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_subtotal 
    CHECK (subtotal >= 0);
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_tax_amount 
    CHECK (tax_amount >= 0);
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_total_amount 
    CHECK (total_amount >= 0);
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_paid_amount 
    CHECK (paid_amount >= 0);

-- Ensure paid_amount does not exceed total_amount for invoices
ALTER TABLE invoices ADD CONSTRAINT chk_invoices_paid_amount_limit 
    CHECK (paid_amount <= total_amount);

-- ============================================
-- Comments for Documentation
-- ============================================
COMMENT ON TABLE business_accounts IS 'Stores business account information for B2B customers';
COMMENT ON TABLE business_contacts IS 'Links users to business accounts with specific contact roles';
COMMENT ON TABLE b2b_pricing_tiers IS 'Defines volume-based pricing tiers for B2B customers by business type';
COMMENT ON TABLE purchase_orders IS 'Tracks purchase order requests and approvals for B2B orders';
COMMENT ON TABLE invoices IS 'Stores invoice information for B2B transactions';

COMMENT ON COLUMN business_accounts.business_type IS 'Type of business: CORPORATE, SME, or ENTERPRISE';
COMMENT ON COLUMN business_accounts.status IS 'Account status: PENDING, APPROVED, REJECTED, or SUSPENDED';
COMMENT ON COLUMN business_contacts.contact_type IS 'Contact role: PRIMARY, BILLING, SHIPPING, or PURCHASER';
COMMENT ON COLUMN b2b_pricing_tiers.business_type IS 'Business type this pricing tier applies to: CORPORATE, SME, or ENTERPRISE';
COMMENT ON COLUMN purchase_orders.status IS 'PO status: PENDING, APPROVED, REJECTED, or COMPLETED';
COMMENT ON COLUMN invoices.status IS 'Invoice status: PENDING, PAID, OVERDUE, or CANCELLED';
