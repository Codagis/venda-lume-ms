-- Campo para definir se o sistema dá baixa automática no estoque ao realizar vendas
ALTER TABLE products ADD COLUMN IF NOT EXISTS deduct_stock_on_sale BOOLEAN NOT NULL DEFAULT true;
COMMENT ON COLUMN products.deduct_stock_on_sale IS 'Se true, o sistema dá baixa no estoque automaticamente ao realizar vendas. Se false, a baixa deve ser feita manualmente.';
