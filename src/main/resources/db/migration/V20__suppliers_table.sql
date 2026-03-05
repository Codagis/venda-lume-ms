-- Cadastro de fornecedores
CREATE TABLE suppliers (
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
    trade_name VARCHAR(255),
    state_registration VARCHAR(50),
    municipal_registration VARCHAR(50),
    contact_name VARCHAR(255),
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    bank_name VARCHAR(100),
    bank_agency VARCHAR(20),
    bank_account VARCHAR(30),
    bank_pix VARCHAR(100),
    payment_terms VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_suppliers PRIMARY KEY (id)
);

CREATE INDEX idx_supplier_tenant_active ON suppliers(tenant_id, active);
CREATE INDEX idx_supplier_tenant_name ON suppliers(tenant_id, name);
CREATE INDEX idx_supplier_tenant_document ON suppliers(tenant_id, document);

COMMENT ON TABLE suppliers IS 'Fornecedores vinculados ao tenant';
