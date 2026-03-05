-- Emite cupom fiscal e comprovante simples
-- Adiciona colunas como nullable primeiro (para tabelas com dados existentes)
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS emits_fiscal_receipt BOOLEAN DEFAULT false;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS emits_simple_receipt BOOLEAN DEFAULT true;
-- Preenche valores nulos e aplica NOT NULL
UPDATE tenants SET emits_fiscal_receipt = false WHERE emits_fiscal_receipt IS NULL;
UPDATE tenants SET emits_simple_receipt = true WHERE emits_simple_receipt IS NULL;
ALTER TABLE tenants ALTER COLUMN emits_fiscal_receipt SET NOT NULL;
ALTER TABLE tenants ALTER COLUMN emits_simple_receipt SET NOT NULL;

COMMENT ON COLUMN tenants.emits_fiscal_receipt IS 'Emite cupom fiscal (requer IE ou IM preenchido)';
COMMENT ON COLUMN tenants.emits_simple_receipt IS 'Emite comprovante simples de venda';
