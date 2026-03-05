-- Garante que colunas emits_fiscal_receipt e emits_simple_receipt existam (reparo se V12 falhou)
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS emits_fiscal_receipt BOOLEAN DEFAULT false;
ALTER TABLE tenants ADD COLUMN IF NOT EXISTS emits_simple_receipt BOOLEAN DEFAULT true;
UPDATE tenants SET emits_fiscal_receipt = false WHERE emits_fiscal_receipt IS NULL;
UPDATE tenants SET emits_simple_receipt = true WHERE emits_simple_receipt IS NULL;
ALTER TABLE tenants ALTER COLUMN emits_fiscal_receipt SET NOT NULL;
ALTER TABLE tenants ALTER COLUMN emits_simple_receipt SET NOT NULL;
