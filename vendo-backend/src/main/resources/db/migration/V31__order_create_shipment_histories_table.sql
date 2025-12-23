-- Create shipment histories table
CREATE TABLE IF NOT EXISTS ec_shipment_histories (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    action VARCHAR(120) NOT NULL,
    description VARCHAR(400),
    user_id BIGINT,
    user_type VARCHAR(191) NOT NULL DEFAULT 'User',
    extras TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shipment_id) REFERENCES ec_shipments(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_shipment_histories_shipment_id ON ec_shipment_histories(shipment_id);
CREATE INDEX IF NOT EXISTS idx_shipment_histories_user_id ON ec_shipment_histories(user_id);
