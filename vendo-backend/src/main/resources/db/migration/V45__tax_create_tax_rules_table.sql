-- Create tax_rules table for location, product type, and business type based tax rules
CREATE TABLE IF NOT EXISTS tax_rules (
    id BIGSERIAL PRIMARY KEY,
    rule_name VARCHAR(191) NOT NULL,
    rule_type VARCHAR(50) NOT NULL, -- LOCATION, PRODUCT_TYPE, BUSINESS_TYPE, DEFAULT
    country VARCHAR(100),
    state VARCHAR(100),
    city VARCHAR(100),
    product_type VARCHAR(50), -- SIMPLE, VARIABLE, GROUPED, DIGITAL, PHYSICAL
    business_type VARCHAR(50), -- CORPORATE, SME, ENTERPRISE, B2C
    tax_percentage DECIMAL(5,2) NOT NULL,
    priority INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    applies_to_b2b BOOLEAN NOT NULL DEFAULT true, -- Whether this rule applies to B2B orders
    exemption_conditions TEXT, -- JSON string for complex exemption conditions
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for tax_rules
CREATE INDEX IF NOT EXISTS idx_tax_rules_type ON tax_rules(rule_type);
CREATE INDEX IF NOT EXISTS idx_tax_rules_country ON tax_rules(country);
CREATE INDEX IF NOT EXISTS idx_tax_rules_state ON tax_rules(state);
CREATE INDEX IF NOT EXISTS idx_tax_rules_product_type ON tax_rules(product_type);
CREATE INDEX IF NOT EXISTS idx_tax_rules_business_type ON tax_rules(business_type);
CREATE INDEX IF NOT EXISTS idx_tax_rules_active ON tax_rules(is_active);
CREATE INDEX IF NOT EXISTS idx_tax_rules_priority ON tax_rules(priority);

-- Create tax_exemptions table for B2B customers
CREATE TABLE IF NOT EXISTS tax_exemptions (
    id BIGSERIAL PRIMARY KEY,
    business_account_id BIGINT REFERENCES business_accounts(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    exemption_type VARCHAR(50) NOT NULL, -- FULL, PARTIAL, PRODUCT_TYPE, LOCATION
    country VARCHAR(100),
    state VARCHAR(100),
    product_type VARCHAR(50),
    exemption_percentage DECIMAL(5,2) DEFAULT 100.00, -- 100% = full exemption
    is_active BOOLEAN NOT NULL DEFAULT true,
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for tax_exemptions
CREATE INDEX IF NOT EXISTS idx_tax_exemptions_business_account ON tax_exemptions(business_account_id);
CREATE INDEX IF NOT EXISTS idx_tax_exemptions_user ON tax_exemptions(user_id);
CREATE INDEX IF NOT EXISTS idx_tax_exemptions_active ON tax_exemptions(is_active);
CREATE INDEX IF NOT EXISTS idx_tax_exemptions_type ON tax_exemptions(exemption_type);

