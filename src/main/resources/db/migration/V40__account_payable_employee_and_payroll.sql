-- Vincular conta a pagar a funcionário e identificar contas de folha (recorrência mensal)
ALTER TABLE account_payable
    ADD COLUMN employee_id UUID NULL,
    ADD COLUMN payroll_reference VARCHAR(7) NULL;

ALTER TABLE account_payable
    ADD CONSTRAINT fk_ap_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE SET NULL;

CREATE INDEX idx_ap_employee ON account_payable(employee_id);
CREATE INDEX idx_ap_payroll ON account_payable(tenant_id, payroll_reference);

COMMENT ON COLUMN account_payable.employee_id IS 'Funcionário quando a conta é salário (folha)';
COMMENT ON COLUMN account_payable.payroll_reference IS 'Mês de referência da folha (YYYY-MM) para contas geradas por funcionário';
