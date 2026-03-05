-- Campos para cupom fiscal e dados da empresa
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS address_street VARCHAR(255);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS address_number VARCHAR(20);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS address_complement VARCHAR(100);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS address_neighborhood VARCHAR(100);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS address_city VARCHAR(100);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS address_state VARCHAR(2);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS address_zip VARCHAR(10);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS state_registration VARCHAR(20);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS municipal_registration VARCHAR(20);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS ecf_series VARCHAR(50);
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS ecf_model VARCHAR(100);

COMMENT ON COLUMN tenants.state_registration IS 'Inscrição estadual (IE)';
COMMENT ON COLUMN tenants.municipal_registration IS 'Inscrição municipal (IM)';
COMMENT ON COLUMN tenants.ecf_series IS 'Número série do equipamento fiscal (ECF)';
COMMENT ON COLUMN tenants.ecf_model IS 'Modelo do equipamento fiscal';
