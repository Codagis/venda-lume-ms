CREATE TABLE IF NOT EXISTS stock_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    movement_type VARCHAR(20) NOT NULL,
    quantity_delta NUMERIC(19,4) NOT NULL,
    quantity_before NUMERIC(19,4),
    quantity_after NUMERIC(19,4),
    sale_id UUID REFERENCES sales(id) ON DELETE SET NULL,
    sale_number VARCHAR(20),
    notes VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_stock_movement_product ON stock_movements(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_movement_tenant ON stock_movements(tenant_id);
CREATE INDEX IF NOT EXISTS idx_stock_movement_created ON stock_movements(created_at);
CREATE INDEX IF NOT EXISTS idx_stock_movement_product_created ON stock_movements(product_id, created_at);

COMMENT ON TABLE stock_movements IS 'Histórico de movimentações de estoque dos produtos';
