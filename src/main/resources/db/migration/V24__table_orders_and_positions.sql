-- Posição da mesa no layout do mapa
ALTER TABLE restaurant_table ADD COLUMN position_x INTEGER;
ALTER TABLE restaurant_table ADD COLUMN position_y INTEGER;
COMMENT ON COLUMN restaurant_table.position_x IS 'Posição X no layout do mapa de mesas';
COMMENT ON COLUMN restaurant_table.position_y IS 'Posição Y no layout do mapa de mesas';

-- Comandas (pedidos de mesa)
CREATE TABLE table_order (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    table_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    opened_at TIMESTAMP WITH TIME ZONE NOT NULL,
    closed_at TIMESTAMP WITH TIME ZONE,
    sale_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_table_order PRIMARY KEY (id),
    CONSTRAINT fk_table_order_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_table_order_table FOREIGN KEY (table_id) REFERENCES restaurant_table(id) ON DELETE CASCADE,
    CONSTRAINT fk_table_order_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE SET NULL
);

CREATE INDEX idx_table_order_tenant ON table_order(tenant_id);
CREATE INDEX idx_table_order_table ON table_order(table_id);
CREATE INDEX idx_table_order_tenant_status ON table_order(tenant_id, status);
CREATE INDEX idx_table_order_sale ON table_order(sale_id);

COMMENT ON TABLE table_order IS 'Comandas (pedidos de mesa)';
COMMENT ON COLUMN table_order.status IS 'OPEN ou CLOSED';
COMMENT ON COLUMN table_order.sale_id IS 'Venda gerada ao fechar a comanda (quando aplicável)';

-- Itens da comanda
CREATE TABLE table_order_item (
    id UUID NOT NULL,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity DECIMAL(19, 4) NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(50),
    item_order INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT pk_table_order_item PRIMARY KEY (id),
    CONSTRAINT fk_table_order_item_order FOREIGN KEY (order_id) REFERENCES table_order(id) ON DELETE CASCADE,
    CONSTRAINT fk_table_order_item_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

CREATE INDEX idx_table_order_item_order ON table_order_item(order_id);
CREATE INDEX idx_table_order_item_product ON table_order_item(product_id);

COMMENT ON TABLE table_order_item IS 'Itens de uma comanda';
