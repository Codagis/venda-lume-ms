-- =============================================================================
-- Script para adicionar colunas faltantes na tabela [tenants]
-- Execute manualmente no banco (PostgreSQL) se o Flyway não tiver rodado
-- ou se a validação do Hibernate falhar com "missing column".
-- =============================================================================

-- NF-e: CRT e Ambiente (Nuvem Fiscal)
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS crt_nfe INTEGER;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS ambiente_nfe VARCHAR(20);

COMMENT ON COLUMN tenants.crt_nfe IS 'CRT para NF-e na Nuvem Fiscal (1 a 4). Se null, usa o mesmo da NFC-e.';
COMMENT ON COLUMN tenants.ambiente_nfe IS 'Ambiente NF-e na Nuvem Fiscal: homologacao ou producao. Se null, usa o mesmo da NFC-e.';

-- Certificado PFX no Google Cloud
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS certificado_pfx_url VARCHAR(500);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS certificado_uploaded_at TIMESTAMP WITH TIME ZONE;

COMMENT ON COLUMN tenants.certificado_pfx_url IS 'URL do certificado PFX no Google Cloud Storage';
COMMENT ON COLUMN tenants.certificado_uploaded_at IS 'Data/hora do ultimo upload do certificado PFX';
