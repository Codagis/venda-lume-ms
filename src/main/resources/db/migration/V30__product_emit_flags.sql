-- Flags por produto para emissão de NFC-e, NF-e e comprovante simples
ALTER TABLE products ADD COLUMN IF NOT EXISTS emits_nfce BOOLEAN DEFAULT true;
ALTER TABLE products ADD COLUMN IF NOT EXISTS emits_nfe BOOLEAN DEFAULT false;
ALTER TABLE products ADD COLUMN IF NOT EXISTS emits_comprovante_simples BOOLEAN DEFAULT true;

UPDATE products SET emits_nfce = true WHERE emits_nfce IS NULL;
UPDATE products SET emits_nfe = false WHERE emits_nfe IS NULL;
UPDATE products SET emits_comprovante_simples = true WHERE emits_comprovante_simples IS NULL;

ALTER TABLE products ALTER COLUMN emits_nfce SET NOT NULL;
ALTER TABLE products ALTER COLUMN emits_nfe SET NOT NULL;
ALTER TABLE products ALTER COLUMN emits_comprovante_simples SET NOT NULL;

COMMENT ON COLUMN products.emits_nfce IS 'Produto deve aparecer em NFC-e (cupom fiscal)';
COMMENT ON COLUMN products.emits_nfe IS 'Produto deve aparecer em NF-e';
COMMENT ON COLUMN products.emits_comprovante_simples IS 'Produto deve aparecer no comprovante simples de venda';
