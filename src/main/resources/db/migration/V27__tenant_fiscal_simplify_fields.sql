-- Campos para integração Fiscal Simplify (CRT, CSC, ambiente)
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS crt INTEGER;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS id_csc INTEGER DEFAULT 0;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS csc VARCHAR(100);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS ambiente_fiscal VARCHAR(20) DEFAULT 'homologacao';

COMMENT ON COLUMN tenants.crt IS 'CRT - Codigo de Regime Tributario (1=Simples Nacional, 2=Excesso sublimite, 3=Regime Normal, 4=MEI)';
COMMENT ON COLUMN tenants.id_csc IS 'ID do CSC (Codigo Seguranca Contribuinte) - geralmente 0';
COMMENT ON COLUMN tenants.csc IS 'CSC - Codigo obtido na SEFAZ do estado para emissao NFC-e';
COMMENT ON COLUMN tenants.ambiente_fiscal IS 'Ambiente Fiscal Simplify: homologacao ou producao';
