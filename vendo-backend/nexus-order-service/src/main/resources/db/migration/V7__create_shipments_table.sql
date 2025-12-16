-- Create shipments table
CREATE TABLE IF NOT EXISTS ec_shipments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_id BIGINT,
    weight DECIMAL(10,2) DEFAULT 0.00,
    shipment_id VARCHAR(120),
    rate_id VARCHAR(120),
    note TEXT,
    status VARCHAR(120) NOT NULL DEFAULT 'pending',
    cod_amount DECIMAL(15,2) DEFAULT 0.00,
    cod_status VARCHAR(60) NOT NULL DEFAULT 'pending',
    cross_checking_status VARCHAR(60) NOT NULL DEFAULT 'pending',
    price DECIMAL(15,2) DEFAULT 0.00,
    store_id BIGINT,
    tracking_id VARCHAR(191),
    shipping_company_name VARCHAR(191),
    tracking_link VARCHAR(191),
    estimate_date_shipped TIMESTAMP,
    date_shipped TIMESTAMP,
    customer_delivered_confirmed_at TIMESTAMP,
    label_url TEXT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES ec_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_shipments_order_id ON ec_shipments(order_id);
CREATE INDEX IF NOT EXISTS idx_shipments_status ON ec_shipments(status);
CREATE INDEX IF NOT EXISTS idx_shipments_tracking_id ON ec_shipments(tracking_id);
