-- Cadastro de clientes
CREATE TABLE customers (
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
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_customers PRIMARY KEY (id)
);

CREATE INDEX idx_customer_tenant_active ON customers(tenant_id, active);
CREATE INDEX idx_customer_tenant_name ON customers(tenant_id, name);
CREATE INDEX idx_customer_tenant_document ON customers(tenant_id, document);

COMMENT ON TABLE customers IS 'Clientes vinculados ao tenant';
