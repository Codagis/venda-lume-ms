-- Notas fiscais de prestadores PJ - armazenamento no Google Cloud Storage (path/URL)
CREATE TABLE contractor_invoices (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    contractor_id UUID NOT NULL,
    reference_month VARCHAR(7) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    nf_number VARCHAR(50),
    nf_key VARCHAR(44),
    description VARCHAR(500),
    file_gcs_path VARCHAR(1024),
    file_original_name VARCHAR(255),
    uploaded_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_contractor_invoices PRIMARY KEY (id),
    CONSTRAINT fk_contractor_invoice_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_contractor_invoice_contractor FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE CASCADE
);

CREATE INDEX idx_contractor_invoice_tenant ON contractor_invoices(tenant_id);
CREATE INDEX idx_contractor_invoice_contractor ON contractor_invoices(contractor_id);
CREATE INDEX idx_contractor_invoice_reference ON contractor_invoices(tenant_id, reference_month);

COMMENT ON TABLE contractor_invoices IS 'Notas fiscais de prestadores PJ - arquivo no GCS, dados para conformidade legal';
COMMENT ON COLUMN contractor_invoices.reference_month IS 'Competência YYYY-MM';
COMMENT ON COLUMN contractor_invoices.nf_key IS 'Chave da NF-e (44 dígitos)';
COMMENT ON COLUMN contractor_invoices.file_gcs_path IS 'Caminho/URL do arquivo no Google Cloud Storage';
