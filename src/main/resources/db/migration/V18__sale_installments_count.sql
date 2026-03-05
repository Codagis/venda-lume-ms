-- Número de parcelas quando pagamento é cartão de crédito
ALTER TABLE sales ADD COLUMN IF NOT EXISTS installments_count INTEGER;

COMMENT ON COLUMN sales.installments_count IS 'Número de parcelas (quando pagamento é cartão de crédito)';
