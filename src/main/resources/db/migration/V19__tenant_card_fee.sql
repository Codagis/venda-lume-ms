-- Taxa da maquininha: percentual ou valor fixo em reais
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS card_fee_type VARCHAR(20);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS card_fee_value NUMERIC(10,4);

COMMENT ON COLUMN tenants.card_fee_type IS 'PERCENTAGE ou FIXED_AMOUNT - tipo da taxa da maquininha';
COMMENT ON COLUMN tenants.card_fee_value IS 'Valor da taxa: percentual (ex: 2.5) ou reais (ex: 0.50) conforme card_fee_type';
