-- Adiciona código do município IBGE (7 dígitos) para integração Fiscal Simplify
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS codigo_municipio VARCHAR(7);
COMMENT ON COLUMN tenants.codigo_municipio IS 'Código do município IBGE (7 dígitos) - ex: 2304400 Fortaleza/CE - necessário para emissão NFC-e';
