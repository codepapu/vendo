-- Migration: Enhanced Order System for B2C, B2B, and Retailer orders
-- Version: V4
-- Description: Adds support for multi-type orders (B2C, B2B, Retailer) with enhanced tracking

-- Add columns to orders table if they don't exist
-- Handle both 'orders' and 'ec_orders' table names (skip when neither exists - they are created in later migrations)
DO $$
DECLARE
    table_name_var TEXT;
BEGIN
    -- Determine which orders table exists; skip this migration block if neither exists yet
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'orders')
       AND NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ec_orders') THEN
        RETURN;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'orders') THEN
        table_name_var := 'orders';
    ELSE
        table_name_var := 'ec_orders';
    END IF;

    -- Add business_account_id column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = table_name_var AND column_name = 'business_account_id'
    ) THEN
        EXECUTE format('ALTER TABLE %I ADD COLUMN business_account_id BIGINT', table_name_var);
        -- Only add foreign key if business_accounts table exists
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'business_accounts') THEN
            EXECUTE format('ALTER TABLE %I ADD CONSTRAINT fk_%s_business_account 
                FOREIGN KEY (business_account_id) REFERENCES business_accounts(id)', 
                table_name_var, REPLACE(table_name_var, '_', ''));
        END IF;
    END IF;

    -- Add order_type column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = table_name_var AND column_name = 'order_type'
    ) THEN
        EXECUTE format('ALTER TABLE %I ADD COLUMN order_type VARCHAR(20) DEFAULT ''B2C'' NOT NULL', table_name_var);
        EXECUTE format('ALTER TABLE %I ADD CONSTRAINT chk_%s_order_type 
            CHECK (order_type IN (''B2C'', ''B2B'', ''RETAILER''))', 
            table_name_var, REPLACE(table_name_var, '_', ''));
    END IF;

    -- Add payment_status column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = table_name_var AND column_name = 'payment_status'
    ) THEN
        EXECUTE format('ALTER TABLE %I ADD COLUMN payment_status VARCHAR(50)', table_name_var);
        EXECUTE format('ALTER TABLE %I ADD CONSTRAINT chk_%s_payment_status 
            CHECK (payment_status IS NULL OR payment_status IN (''PENDING'', ''PAID'', ''PARTIAL'', ''REFUNDED'', ''FAILED''))', 
            table_name_var, REPLACE(table_name_var, '_', ''));
    END IF;
END $$;

-- Add columns to order_items / ec_order_products if they exist (order_items/ec_order_products created in later migrations)
DO $$
BEGIN
    -- If order_items exists, add retailer_id and commission_amount if missing
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'order_items') THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'order_items' AND column_name = 'retailer_id'
        ) THEN
            ALTER TABLE order_items ADD COLUMN retailer_id BIGINT;
            IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'retailers') THEN
                ALTER TABLE order_items ADD CONSTRAINT fk_order_items_retailer 
                    FOREIGN KEY (retailer_id) REFERENCES retailers(id);
            END IF;
        END IF;
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'order_items' AND column_name = 'commission_amount'
        ) THEN
            ALTER TABLE order_items ADD COLUMN commission_amount DECIMAL(15,2) DEFAULT 0;
        END IF;
    END IF;

    -- Also handle ec_order_products if it exists (add retailer_id if missing)
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ec_order_products') THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'ec_order_products' AND column_name = 'retailer_id'
        ) THEN
            ALTER TABLE ec_order_products ADD COLUMN retailer_id BIGINT;
            -- Only add foreign key if retailers table exists
            IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'retailers') THEN
                ALTER TABLE ec_order_products ADD CONSTRAINT fk_ec_order_products_retailer 
                    FOREIGN KEY (retailer_id) REFERENCES retailers(id);
            END IF;
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'ec_order_products' AND column_name = 'commission_amount'
        ) THEN
            ALTER TABLE ec_order_products ADD COLUMN commission_amount DECIMAL(15,2) DEFAULT 0;
        END IF;
    END IF;
END $$;

-- Add columns to shipments / ec_shipments if they exist (ec_shipments created in later migrations)
DO $$
BEGIN
    -- If shipments exists, add missing columns
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'shipments') THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'shipments' AND column_name = 'tracking_number'
        ) THEN
            ALTER TABLE shipments ADD COLUMN tracking_number VARCHAR(255);
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'shipments' AND column_name = 'carrier'
        ) THEN
            ALTER TABLE shipments ADD COLUMN carrier VARCHAR(100);
        END IF;
    END IF;
    
    -- Also handle ec_shipments if it exists (add tracking_number and carrier if missing)
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ec_shipments') THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'ec_shipments' AND column_name = 'tracking_number'
        ) THEN
            ALTER TABLE ec_shipments ADD COLUMN tracking_number VARCHAR(255);
        END IF;
        
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'ec_shipments' AND column_name = 'carrier'
        ) THEN
            ALTER TABLE ec_shipments ADD COLUMN carrier VARCHAR(100);
        END IF;
    END IF;
END $$;

-- Add indexes for performance optimization
-- Order items indexes - handle both table names
DO $$
BEGIN
    -- Create indexes on order_items table
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'order_items') THEN
        CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
        CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);
        CREATE INDEX IF NOT EXISTS idx_order_items_retailer_id ON order_items(retailer_id);
        CREATE INDEX IF NOT EXISTS idx_order_items_order_retailer ON order_items(order_id, retailer_id);
    END IF;
    
    -- Create indexes on ec_order_products table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ec_order_products') THEN
        CREATE INDEX IF NOT EXISTS idx_ec_order_products_order_id ON ec_order_products(order_id);
        CREATE INDEX IF NOT EXISTS idx_ec_order_products_product_id ON ec_order_products(product_id);
        CREATE INDEX IF NOT EXISTS idx_ec_order_products_retailer_id ON ec_order_products(retailer_id);
        CREATE INDEX IF NOT EXISTS idx_ec_order_products_order_retailer 
            ON ec_order_products(order_id, retailer_id) WHERE retailer_id IS NOT NULL;
    END IF;
END $$;

-- Shipments indexes - handle both table names
DO $$
BEGIN
    -- Create indexes on shipments table
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'shipments') THEN
        CREATE INDEX IF NOT EXISTS idx_shipments_order_id ON shipments(order_id);
        CREATE INDEX IF NOT EXISTS idx_shipments_tracking_number ON shipments(tracking_number);
        CREATE INDEX IF NOT EXISTS idx_shipments_carrier ON shipments(carrier);
        CREATE INDEX IF NOT EXISTS idx_shipments_status ON shipments(status);
        CREATE INDEX IF NOT EXISTS idx_shipments_shipping_method_id ON shipments(shipping_method_id);
    END IF;
    
    -- Create indexes on ec_shipments table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ec_shipments') THEN
        CREATE INDEX IF NOT EXISTS idx_ec_shipments_tracking_number ON ec_shipments(tracking_number);
        CREATE INDEX IF NOT EXISTS idx_ec_shipments_carrier ON ec_shipments(carrier);
    END IF;
END $$;

-- Orders table indexes (for new columns) - handle both table names
DO $$
DECLARE
    table_name_var TEXT;
BEGIN
    -- Create indexes on orders table
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'orders') THEN
        CREATE INDEX IF NOT EXISTS idx_orders_business_account_id ON orders(business_account_id);
        CREATE INDEX IF NOT EXISTS idx_orders_order_type ON orders(order_type);
        CREATE INDEX IF NOT EXISTS idx_orders_payment_status ON orders(payment_status);
        CREATE INDEX IF NOT EXISTS idx_orders_type_status ON orders(order_type, status);
        CREATE INDEX IF NOT EXISTS idx_orders_business_type_status 
            ON orders(business_account_id, order_type, status) WHERE business_account_id IS NOT NULL;
    END IF;
    
    -- Create indexes on ec_orders table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ec_orders') THEN
        CREATE INDEX IF NOT EXISTS idx_ec_orders_business_account_id ON ec_orders(business_account_id);
        CREATE INDEX IF NOT EXISTS idx_ec_orders_order_type ON ec_orders(order_type);
        CREATE INDEX IF NOT EXISTS idx_ec_orders_payment_status ON ec_orders(payment_status);
        CREATE INDEX IF NOT EXISTS idx_ec_orders_type_status ON ec_orders(order_type, status);
        CREATE INDEX IF NOT EXISTS idx_ec_orders_business_type_status 
            ON ec_orders(business_account_id, order_type, status) WHERE business_account_id IS NOT NULL;
    END IF;
END $$;

-- Comments for documentation
DO $$
BEGIN
    -- Add comments to orders table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'orders') THEN
        COMMENT ON COLUMN orders.business_account_id IS 'Reference to business account for B2B orders';
        COMMENT ON COLUMN orders.order_type IS 'Order type: B2C (consumer), B2B (business), RETAILER (marketplace)';
        COMMENT ON COLUMN orders.payment_status IS 'Payment status: PENDING, PAID, PARTIAL, REFUNDED, FAILED';
    END IF;
    
    -- Add comments to ec_orders table if it exists
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ec_orders') THEN
        COMMENT ON COLUMN ec_orders.business_account_id IS 'Reference to business account for B2B orders';
        COMMENT ON COLUMN ec_orders.order_type IS 'Order type: B2C (consumer), B2B (business), RETAILER (marketplace)';
        COMMENT ON COLUMN ec_orders.payment_status IS 'Payment status: PENDING, PAID, PARTIAL, REFUNDED, FAILED';
    END IF;
END $$;

-- Comments for order_items and shipments (only when tables exist)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'order_items') THEN
        EXECUTE 'COMMENT ON TABLE order_items IS ''Order line items with support for multi-vendor marketplace orders''';
        EXECUTE 'COMMENT ON COLUMN order_items.retailer_id IS ''Retailer/vendor ID for marketplace orders (NULL for direct sales)''';
        EXECUTE 'COMMENT ON COLUMN order_items.commission_amount IS ''Platform commission amount for marketplace orders''';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'shipments') THEN
        EXECUTE 'COMMENT ON TABLE shipments IS ''Shipment tracking information for orders''';
        EXECUTE 'COMMENT ON COLUMN shipments.tracking_number IS ''Carrier tracking number for shipment''';
        EXECUTE 'COMMENT ON COLUMN shipments.carrier IS ''Shipping carrier name (e.g., FedEx, UPS, DHL)''';
        EXECUTE 'COMMENT ON COLUMN shipments.status IS ''Shipment status: PENDING, IN_TRANSIT, DELIVERED, FAILED''';
    END IF;
END $$;

-- Add missing foreign key constraints if dependent tables were created after this migration
DO $$
BEGIN
    -- Add retailer foreign key to order_items if retailers table exists and constraint is missing
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'retailers') 
       AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'order_items')
       AND EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'order_items' AND column_name = 'retailer_id')
       AND NOT EXISTS (
           SELECT 1 FROM information_schema.table_constraints 
           WHERE table_name = 'order_items' AND constraint_name = 'fk_order_items_retailer'
       ) THEN
        ALTER TABLE order_items ADD CONSTRAINT fk_order_items_retailer 
            FOREIGN KEY (retailer_id) REFERENCES retailers(id) ON DELETE SET NULL;
    END IF;
    
    -- Add shipping_method foreign key to shipments if shipping_methods table exists and constraint is missing
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'shipping_methods') 
       AND EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'shipments')
       AND EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'shipments' AND column_name = 'shipping_method_id')
       AND NOT EXISTS (
           SELECT 1 FROM information_schema.table_constraints 
           WHERE table_name = 'shipments' AND constraint_name = 'fk_shipments_shipping_method'
       ) THEN
        ALTER TABLE shipments ADD CONSTRAINT fk_shipments_shipping_method 
            FOREIGN KEY (shipping_method_id) REFERENCES shipping_methods(id);
    END IF;
    
    -- Add business_account foreign key to orders/ec_orders if business_accounts table exists and constraint is missing
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'business_accounts') THEN
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'orders')
           AND EXISTS (SELECT 1 FROM information_schema.columns 
                       WHERE table_name = 'orders' AND column_name = 'business_account_id')
           AND NOT EXISTS (
               SELECT 1 FROM information_schema.table_constraints 
               WHERE table_name = 'orders' AND constraint_name LIKE 'fk_%_business_account'
           ) THEN
            ALTER TABLE orders ADD CONSTRAINT fk_orders_business_account 
                FOREIGN KEY (business_account_id) REFERENCES business_accounts(id);
        END IF;
        
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ec_orders')
           AND EXISTS (SELECT 1 FROM information_schema.columns 
                       WHERE table_name = 'ec_orders' AND column_name = 'business_account_id')
           AND NOT EXISTS (
               SELECT 1 FROM information_schema.table_constraints 
               WHERE table_name = 'ec_orders' AND constraint_name LIKE 'fk_%_business_account'
           ) THEN
            ALTER TABLE ec_orders ADD CONSTRAINT fk_ecorders_business_account 
                FOREIGN KEY (business_account_id) REFERENCES business_accounts(id);
        END IF;
    END IF;
END $$;
