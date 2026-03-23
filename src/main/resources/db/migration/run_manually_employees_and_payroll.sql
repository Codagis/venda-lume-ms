-- =============================================================================
-- SCRIPT: Funcionários e Folha de Pagamento (V39 + V40 + V41)
-- Execute na ordem no banco PostgreSQL (conectado ao schema da aplicação).
-- Se usar Flyway, NÃO rode este arquivo: as migrations V39, V40, V41 rodam sozinhas.
-- =============================================================================

-- ---------- V39: Tabela de funcionários ----------
CREATE TABLE IF NOT EXISTS employees (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    document VARCHAR(20),
    email VARCHAR(255),
    phone VARCHAR(20),
    phone_alt VARCHAR(20),
    address_street VARCHAR(255),
    address_number VARCHAR(20),
    address_complement VARCHAR(100),
    address_neighborhood VARCHAR(100),
    address_city VARCHAR(100),
    address_state VARCHAR(2),
    address_zip VARCHAR(10),
    role VARCHAR(100),
    salary NUMERIC(19, 4) NOT NULL DEFAULT 0,
    payment_day INTEGER NOT NULL DEFAULT 5,
    bank_name VARCHAR(100),
    bank_agency VARCHAR(20),
    bank_account VARCHAR(30),
    bank_pix VARCHAR(100),
    hire_date DATE,
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_employees PRIMARY KEY (id),
    CONSTRAINT fk_employee_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT chk_employee_payment_day CHECK (payment_day >= 1 AND payment_day <= 28),
    CONSTRAINT chk_employee_salary CHECK (salary >= 0)
);

CREATE INDEX IF NOT EXISTS idx_employee_tenant_active ON employees(tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_employee_tenant_name ON employees(tenant_id, name);
CREATE INDEX IF NOT EXISTS idx_employee_tenant_document ON employees(tenant_id, document);

COMMENT ON TABLE employees IS 'Funcionários do tenant para folha de pagamento e contas a pagar recorrentes';


-- ---------- V40: Colunas de funcionário/folha em account_payable ----------
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'account_payable' AND column_name = 'employee_id'
  ) THEN
    ALTER TABLE account_payable
      ADD COLUMN employee_id UUID NULL,
      ADD COLUMN payroll_reference VARCHAR(7) NULL;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE table_name = 'account_payable' AND constraint_name = 'fk_ap_employee'
  ) THEN
    ALTER TABLE account_payable
      ADD CONSTRAINT fk_ap_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE SET NULL;
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_ap_employee ON account_payable(employee_id);
CREATE INDEX IF NOT EXISTS idx_ap_payroll ON account_payable(tenant_id, payroll_reference);

COMMENT ON COLUMN account_payable.employee_id IS 'Funcionário quando a conta é salário (folha)';
COMMENT ON COLUMN account_payable.payroll_reference IS 'Mês de referência da folha (YYYY-MM) para contas geradas por funcionário';


-- ---------- V41: Módulo e permissões Funcionários ----------
INSERT INTO permissions (id, code, name, description, module, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'EMPLOYEE_VIEW', 'Visualizar funcionários', 'Ver listagem e detalhes de funcionários', 'EMPLOYEES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'EMPLOYEE_CREATE', 'Criar funcionários', 'Cadastrar novos funcionários', 'EMPLOYEES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'EMPLOYEE_EDIT', 'Editar funcionários', 'Alterar dados de funcionários', 'EMPLOYEES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'EMPLOYEE_DELETE', 'Excluir funcionários', 'Remover funcionários', 'EMPLOYEES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'PAYROLL_VIEW', 'Ver folha de pagamento', 'Consultar e exportar folha de pagamento', 'EMPLOYEES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'PAYROLL_GENERATE', 'Gerar folha do mês', 'Gerar contas a pagar dos funcionários', 'EMPLOYEES', NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;

INSERT INTO modules (id, code, name, description, icon, route, component, display_order, view_permission_code, active, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'EMPLOYEES', 'Funcionários', 'Cadastro de funcionários e folha de pagamento', 'TeamOutlined', '/employees', 'Employees', 15, 'EMPLOYEE_VIEW', true, NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;
