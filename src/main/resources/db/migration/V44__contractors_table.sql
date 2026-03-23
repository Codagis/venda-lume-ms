-- Prestadores de serviço PJ (pessoa jurídica) - contratação conforme Lei (NFS-e, guarda de documentos)
CREATE TABLE contractors (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255),
    cnpj VARCHAR(18),
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
    bank_name VARCHAR(100),
    bank_agency VARCHAR(20),
    bank_account VARCHAR(30),
    bank_pix VARCHAR(100),
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_contractors PRIMARY KEY (id),
    CONSTRAINT fk_contractor_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE INDEX idx_contractor_tenant_active ON contractors(tenant_id, active);
CREATE INDEX idx_contractor_tenant_name ON contractors(tenant_id, name);
CREATE INDEX idx_contractor_tenant_cnpj ON contractors(tenant_id, cnpj);

COMMENT ON TABLE contractors IS 'Prestadores de serviço PJ registrados pela empresa (Lei - guarda de NFS-e/NF-e)';
