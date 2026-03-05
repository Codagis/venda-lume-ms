-- Tipo de integração do cartão na NFC-e: 1 = TEF, 2 = POS (obrigatório quando pagamento é cartão)
ALTER TABLE sales ADD COLUMN IF NOT EXISTS card_integration_type INTEGER NULL;
COMMENT ON COLUMN sales.card_integration_type IS 'Tipo integração cartão NFC-e: 1=TEF, 2=POS';
