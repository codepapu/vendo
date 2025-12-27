-- Create auto_reorder_settings table
CREATE TABLE IF NOT EXISTS auto_reorder_settings (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    retailer_id BIGINT REFERENCES retailers(id) ON DELETE SET NULL,
    warehouse_id BIGINT REFERENCES warehouses(id) ON DELETE SET NULL,
    reorder_point INTEGER NOT NULL,
    reorder_quantity INTEGER NOT NULL,
    reorder_strategy VARCHAR(50),
    is_enabled BOOLEAN NOT NULL DEFAULT true,
    last_reorder_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_auto_reorder_product_retailer ON auto_reorder_settings(product_id, retailer_id);

