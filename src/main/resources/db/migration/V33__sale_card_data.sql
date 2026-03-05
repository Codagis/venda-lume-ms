-- Dados de cartão na venda (para NFC-e: bandeira, autorização; CNPJ vem da maquininha)
ALTER TABLE sales ADD COLUMN IF NOT EXISTS card_machine_id UUID NULL;
ALTER TABLE sales ADD COLUMN IF NOT EXISTS card_brand VARCHAR(2) NULL;
ALTER TABLE sales ADD COLUMN IF NOT EXISTS card_authorization VARCHAR(20) NULL;
COMMENT ON COLUMN sales.card_machine_id IS 'Maquininha usada no pagamento (traz CNPJ da adquirente para NFC-e)';
COMMENT ON COLUMN sales.card_brand IS 'Bandeira do cartão para NFC-e: 01=Visa, 02=Master, 03=Amex, 04=Sorocred, 99=Outros';
COMMENT ON COLUMN sales.card_authorization IS 'Número da autorização da transação (1-20 caracteres)';
