-- Configurações de parcelamento para cartão de crédito
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS max_installments INTEGER NOT NULL DEFAULT 12;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS max_installments_no_interest INTEGER NOT NULL DEFAULT 1;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS interest_rate_percent NUMERIC(5,2) NOT NULL DEFAULT 0;

COMMENT ON COLUMN tenants.max_installments IS 'Quantidade máxima de parcelas no cartão de crédito';
COMMENT ON COLUMN tenants.max_installments_no_interest IS 'Quantidade máxima de parcelas sem juros';
COMMENT ON COLUMN tenants.interest_rate_percent IS 'Percentual de juros por parcela (ex: 2.99)';
