-- Migration: V5__create_inventory_tables.sql
-- Description: Create database tables for multi-warehouse inventory management
-- Tables: warehouses, inventory, stock_movements

-- Warehouses
CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    address_id BIGINT REFERENCES addresses(id),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inventory
CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    retailer_id BIGINT REFERENCES retailers(id),
    warehouse_id BIGINT REFERENCES warehouses(id),
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER DEFAULT 0,
    available_quantity INTEGER GENERATED ALWAYS AS (quantity - reserved_quantity) STORED,
    low_stock_threshold INTEGER DEFAULT 10,
    reorder_point INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(product_id, retailer_id, warehouse_id)
);

-- Stock Movements
CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT REFERENCES inventory(id),
    movement_type VARCHAR(50) NOT NULL, -- IN, OUT, ADJUSTMENT, RETURN
    quantity INTEGER NOT NULL,
    reference_type VARCHAR(50), -- ORDER, RETURN, ADJUSTMENT
    reference_id BIGINT,
    notes TEXT,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
-- Indexes on product_id
CREATE INDEX idx_inventory_product_id ON inventory(product_id);

-- Indexes on retailer_id
CREATE INDEX idx_inventory_retailer_id ON inventory(retailer_id);

-- Indexes on warehouse_id
CREATE INDEX idx_inventory_warehouse_id ON inventory(warehouse_id);
CREATE INDEX idx_stock_movements_inventory_id ON stock_movements(inventory_id);

-- Composite indexes on product_id, retailer_id, and warehouse_id combinations
CREATE INDEX idx_inventory_product_retailer ON inventory(product_id, retailer_id);
CREATE INDEX idx_inventory_product_warehouse ON inventory(product_id, warehouse_id);
CREATE INDEX idx_inventory_retailer_warehouse ON inventory(retailer_id, warehouse_id);
CREATE INDEX idx_inventory_product_retailer_warehouse ON inventory(product_id, retailer_id, warehouse_id);

-- Additional useful indexes
CREATE INDEX idx_warehouses_code ON warehouses(code);
CREATE INDEX idx_warehouses_is_active ON warehouses(is_active);
CREATE INDEX idx_inventory_available_quantity ON inventory(available_quantity);
CREATE INDEX idx_inventory_low_stock ON inventory(product_id, retailer_id, warehouse_id) WHERE available_quantity <= low_stock_threshold;
CREATE INDEX idx_stock_movements_movement_type ON stock_movements(movement_type);
CREATE INDEX idx_stock_movements_reference ON stock_movements(reference_type, reference_id);
CREATE INDEX idx_stock_movements_created_at ON stock_movements(created_at);
CREATE INDEX idx_stock_movements_created_by ON stock_movements(created_by);
