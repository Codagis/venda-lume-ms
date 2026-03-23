-- Parcelas por maquininha: máximo de parcelas e parcelas sem juros
ALTER TABLE card_machines
    ADD COLUMN IF NOT EXISTS max_installments INTEGER,
    ADD COLUMN IF NOT EXISTS max_installments_no_interest INTEGER,
    ADD COLUMN IF NOT EXISTS interest_rate_percent NUMERIC(8, 4);

COMMENT ON COLUMN card_machines.max_installments IS 'Quantidade máxima de parcelas para cartão de crédito';
COMMENT ON COLUMN card_machines.max_installments_no_interest IS 'Quantidade de parcelas sem juros';
COMMENT ON COLUMN card_machines.interest_rate_percent IS 'Taxa de juros ao mês (%) para parcelas com juros';
