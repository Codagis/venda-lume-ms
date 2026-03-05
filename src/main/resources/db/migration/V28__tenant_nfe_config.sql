-- Configuração NF-e na Nuvem Fiscal (CRT e Ambiente específicos da NF-e)
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS crt_nfe INTEGER;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS ambiente_nfe VARCHAR(20);

COMMENT ON COLUMN tenants.crt_nfe IS 'CRT para NF-e na Nuvem Fiscal (1 a 4). Se null, usa o mesmo da NFC-e.';
COMMENT ON COLUMN tenants.ambiente_nfe IS 'Ambiente NF-e na Nuvem Fiscal: homologacao ou producao. Se null, usa o mesmo da NFC-e.';
