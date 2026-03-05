-- CNPJ da adquirente na maquininha (para NFC-e quando pagamento é cartão)
ALTER TABLE card_machines ADD COLUMN IF NOT EXISTS acquirer_cnpj VARCHAR(14) NULL;
COMMENT ON COLUMN card_machines.acquirer_cnpj IS 'CNPJ da adquirente (instituição de pagamento) para envio na NFC-e quando pagamento for cartão';
