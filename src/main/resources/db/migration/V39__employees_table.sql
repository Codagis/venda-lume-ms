-- Cadastro de funcionários (salário, dia de vencimento para geração de contas a pagar)
CREATE TABLE employees (
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

CREATE INDEX idx_employee_tenant_active ON employees(tenant_id, active);
CREATE INDEX idx_employee_tenant_name ON employees(tenant_id, name);
CREATE INDEX idx_employee_tenant_document ON employees(tenant_id, document);

COMMENT ON TABLE employees IS 'Funcionários do tenant para folha de pagamento e contas a pagar recorrentes';
