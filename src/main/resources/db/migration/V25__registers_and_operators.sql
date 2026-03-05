-- Pontos de venda (caixas)
CREATE TABLE registers (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(30),
    equipment_type VARCHAR(30) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_register PRIMARY KEY (id),
    CONSTRAINT fk_register_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uk_register_tenant_name ON registers(tenant_id, name);
CREATE INDEX idx_register_tenant ON registers(tenant_id);
CREATE INDEX idx_register_tenant_active ON registers(tenant_id, active);

COMMENT ON TABLE registers IS 'Pontos de venda (caixas): Caixa 1, Caixa 2, etc.';

-- Operadores por caixa
CREATE TABLE register_operators (
    register_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_register_operator PRIMARY KEY (register_id, user_id),
    CONSTRAINT fk_register_operator_register FOREIGN KEY (register_id) REFERENCES registers(id) ON DELETE CASCADE,
    CONSTRAINT fk_register_operator_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_register_operator_user ON register_operators(user_id);

COMMENT ON TABLE register_operators IS 'Usuarios autorizados a operar cada ponto de venda';
