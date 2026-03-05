-- Logo da empresa (URL no Google Cloud Storage ou qualquer URL)
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS logo_url VARCHAR(500);
COMMENT ON COLUMN tenants.logo_url IS 'URL da logo da empresa (ex.: upload GCS)';
