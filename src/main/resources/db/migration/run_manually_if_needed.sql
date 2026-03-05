-- Execute este script manualmente no PostgreSQL se a aplicação falhar com
-- "missing column" (ex: deduct_stock_on_sale, installments_count, max_installments, etc.)
-- Use: psql -U seu_usuario -d seu_banco -f run_manually_if_needed.sql
-- Ou execute no pgAdmin / DBeaver. Depois reinicie a aplicação.

-- V14: Coluna deduct_stock_on_sale
ALTER TABLE products ADD COLUMN IF NOT EXISTS deduct_stock_on_sale BOOLEAN NOT NULL DEFAULT true;
COMMENT ON COLUMN products.deduct_stock_on_sale IS 'Se true, o sistema dá baixa no estoque automaticamente ao realizar vendas.';

-- V15: Tabela stock_movements
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

-- V16: Permissões e módulo Estoque
INSERT INTO permissions (id, code, name, description, module, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'STOCK_VIEW', 'Visualizar estoque', 'Ver gestão de estoque e movimentações', 'STOCK', NOW(), NOW(), 0),
    (gen_random_uuid(), 'STOCK_MANAGE', 'Gerenciar estoque', 'Registrar entradas, saídas e ajustes', 'STOCK', NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;

INSERT INTO modules (id, code, name, description, icon, route, component, display_order, view_permission_code, active, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'STOCK', 'Estoque', 'Controle de estoque e movimentações', 'InboxOutlined', '/stock', 'Stock', 11, 'STOCK_VIEW', true, NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;

-- V17: Configurações de parcelamento (tenants)
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS max_installments INTEGER NOT NULL DEFAULT 12;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS max_installments_no_interest INTEGER NOT NULL DEFAULT 1;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS interest_rate_percent NUMERIC(5,2) NOT NULL DEFAULT 0;
COMMENT ON COLUMN tenants.max_installments IS 'Quantidade máxima de parcelas no cartão de crédito';
COMMENT ON COLUMN tenants.max_installments_no_interest IS 'Quantidade máxima de parcelas sem juros';
COMMENT ON COLUMN tenants.interest_rate_percent IS 'Percentual de juros por parcela (ex: 2.99)';

-- V18: Parcelas na venda
ALTER TABLE sales ADD COLUMN IF NOT EXISTS installments_count INTEGER;
COMMENT ON COLUMN sales.installments_count IS 'Número de parcelas (quando pagamento é cartão de crédito)';

-- V19: Taxa maquininha (tenants)
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS card_fee_type VARCHAR(20);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS card_fee_value NUMERIC(10,4);

-- V26: Código município IBGE (Fiscal Simplify)
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS codigo_municipio VARCHAR(7);
COMMENT ON COLUMN tenants.codigo_municipio IS 'Codigo do municipio IBGE (7 digitos) - ex: 2304400 Fortaleza/CE - necessario para emissao NFC-e';

-- V27: CRT, CSC e ambiente Fiscal Simplify
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS crt INTEGER;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS id_csc INTEGER DEFAULT 0;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS csc VARCHAR(100);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS ambiente_fiscal VARCHAR(20) DEFAULT 'homologacao';
