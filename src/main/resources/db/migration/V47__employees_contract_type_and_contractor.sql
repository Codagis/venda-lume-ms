-- Modalidade de contratação do funcionário: CLT ou PJ.
-- Quando PJ, opcionalmente vincula ao prestador (contractors) para exigir NF no pagamento.
ALTER TABLE employees
    ADD COLUMN IF NOT EXISTS contract_type VARCHAR(10) NOT NULL DEFAULT 'CLT',
    ADD COLUMN IF NOT EXISTS contractor_id UUID NULL;

COMMENT ON COLUMN employees.contract_type IS 'CLT ou PJ';
COMMENT ON COLUMN employees.contractor_id IS 'Prestador PJ vinculado (quando contract_type = PJ)';

ALTER TABLE employees
    ADD CONSTRAINT fk_employee_contractor
    FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_employee_contractor ON employees(contractor_id);
