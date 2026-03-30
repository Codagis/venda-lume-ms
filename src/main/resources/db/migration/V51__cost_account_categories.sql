-- Categorias de contas a pagar / a receber por empresa
CREATE TABLE cost_account_category (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    kind VARCHAR(20) NOT NULL,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_cost_account_category PRIMARY KEY (id),
    CONSTRAINT fk_ccc_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT chk_ccc_kind CHECK (kind IN ('PAYABLE', 'RECEIVABLE'))
);

CREATE INDEX idx_ccc_tenant_kind ON cost_account_category(tenant_id, kind);
CREATE INDEX idx_ccc_tenant_kind_active ON cost_account_category(tenant_id, kind, active);

CREATE UNIQUE INDEX uq_ccc_tenant_kind_name_lower ON cost_account_category (tenant_id, kind, lower(name));
