-- Execute este script manualmente no banco se a tabela sale_audit ainda não existir
-- (por exemplo, se a migração Flyway V35 não tiver sido aplicada).

-- PostgreSQL
CREATE TABLE IF NOT EXISTS sale_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id UUID NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    event_type VARCHAR(20) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    user_id UUID NULL,
    user_name VARCHAR(255) NULL,
    description TEXT NULL
);

CREATE INDEX IF NOT EXISTS idx_sale_audit_sale_id ON sale_audit(sale_id);
CREATE INDEX IF NOT EXISTS idx_sale_audit_occurred_at ON sale_audit(occurred_at);

COMMENT ON TABLE sale_audit IS 'Auditoria da venda: criacao, edicao, cancelamento';
