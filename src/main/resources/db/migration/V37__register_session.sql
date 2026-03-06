CREATE TABLE register_session (
    id UUID NOT NULL,
    register_id UUID NOT NULL,
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    opened_at TIMESTAMP WITH TIME ZONE NOT NULL,
    closed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_register_session PRIMARY KEY (id),
    CONSTRAINT fk_register_session_register FOREIGN KEY (register_id) REFERENCES registers(id) ON DELETE CASCADE,
    CONSTRAINT fk_register_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_register_session_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE INDEX idx_register_session_register ON register_session(register_id);
CREATE INDEX idx_register_session_user ON register_session(user_id);
CREATE INDEX idx_register_session_tenant ON register_session(tenant_id);
CREATE INDEX idx_register_session_opened ON register_session(opened_at);
CREATE INDEX idx_register_session_register_opened ON register_session(register_id, opened_at);

COMMENT ON TABLE register_session IS 'Auditoria de sessoes do PDV: abertura e fechamento por operador';
