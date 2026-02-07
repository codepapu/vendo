-- Add columns required by JPA entities mapping to ec_orders and ec_products

-- ec_orders: add columns for Order entity
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ec_orders' AND column_name = 'currency') THEN
        ALTER TABLE ec_orders ADD COLUMN currency VARCHAR(3) DEFAULT 'USD';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ec_orders' AND column_name = 'payment_method') THEN
        ALTER TABLE ec_orders ADD COLUMN payment_method VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ec_orders' AND column_name = 'notes') THEN
        ALTER TABLE ec_orders ADD COLUMN notes TEXT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ec_orders' AND column_name = 'cancelled_at') THEN
        ALTER TABLE ec_orders ADD COLUMN cancelled_at TIMESTAMP;
    END IF;
END $$;

-- ec_products: add slug if missing (required by Product entity)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'ec_products' AND column_name = 'slug') THEN
        ALTER TABLE ec_products ADD COLUMN slug VARCHAR(255) UNIQUE;
        CREATE INDEX IF NOT EXISTS idx_ec_products_slug ON ec_products(slug);
    END IF;
END $$;
