-- URL do certificado PFX no Google Cloud Storage
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS certificado_pfx_url VARCHAR(500);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS certificado_uploaded_at TIMESTAMP WITH TIME ZONE;

COMMENT ON COLUMN tenants.certificado_pfx_url IS 'URL do certificado PFX no Google Cloud Storage';
COMMENT ON COLUMN tenants.certificado_uploaded_at IS 'Data/hora do ultimo upload do certificado PFX';
