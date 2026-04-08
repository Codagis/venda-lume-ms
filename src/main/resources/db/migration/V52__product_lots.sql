-- Tabela: product_lots
-- Lotes por produto (controle opcional por lote)
CREATE TABLE product_lots (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    product_id UUID NOT NULL,
    lot_code VARCHAR(60) NOT NULL,
    expires_at DATE,
    quantity DECIMAL(19, 4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_product_lots PRIMARY KEY (id),
    CONSTRAINT fk_product_lots_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_product_lots_tenant_product ON product_lots(tenant_id, product_id);
CREATE INDEX idx_product_lots_expires_at ON product_lots(expires_at);
CREATE INDEX idx_product_lots_lot_code ON product_lots(lot_code);

COMMENT ON TABLE product_lots IS 'Lotes de produtos (opcional) para controle por validade e rastreabilidade';
COMMENT ON COLUMN product_lots.lot_code IS 'Código/identificador do lote';
COMMENT ON COLUMN product_lots.expires_at IS 'Data de validade do lote (opcional)';
COMMENT ON COLUMN product_lots.quantity IS 'Quantidade disponível neste lote';
