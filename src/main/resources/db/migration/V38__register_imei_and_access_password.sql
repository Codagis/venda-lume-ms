-- IMEI: identificador único do equipamento (tablet/celular) vinculado ao PDV
-- Senha de acesso: cada PDV pode ter uma senha para liberar o uso no equipamento
ALTER TABLE registers
    ADD COLUMN IF NOT EXISTS imei VARCHAR(100),
    ADD COLUMN IF NOT EXISTS access_password_hash VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uk_register_imei ON registers(imei) WHERE imei IS NOT NULL;
COMMENT ON COLUMN registers.imei IS 'Identificador único do equipamento (gerado pelo dispositivo); um PDV por equipamento';
COMMENT ON COLUMN registers.access_password_hash IS 'Hash BCrypt da senha de acesso ao PDV neste equipamento';
