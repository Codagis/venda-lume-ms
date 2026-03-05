-- Contas a pagar
CREATE TABLE account_payable (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    supplier_id UUID,
    description VARCHAR(255) NOT NULL,
    reference VARCHAR(100),
    category VARCHAR(50),
    due_date DATE NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    paid_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_date DATE,
    payment_method VARCHAR(30),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_account_payable PRIMARY KEY (id),
    CONSTRAINT fk_ap_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_ap_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL
);

CREATE INDEX idx_ap_tenant_status ON account_payable(tenant_id, status);
CREATE INDEX idx_ap_tenant_due_date ON account_payable(tenant_id, due_date);
CREATE INDEX idx_ap_supplier ON account_payable(supplier_id);

-- Contas a receber
CREATE TABLE account_receivable (
    id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    customer_id UUID,
    sale_id UUID,
    description VARCHAR(255) NOT NULL,
    reference VARCHAR(100),
    category VARCHAR(50),
    due_date DATE NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    received_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    receipt_date DATE,
    payment_method VARCHAR(30),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_account_receivable PRIMARY KEY (id),
    CONSTRAINT fk_ar_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_ar_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    CONSTRAINT fk_ar_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE SET NULL
);

CREATE INDEX idx_ar_tenant_status ON account_receivable(tenant_id, status);
CREATE INDEX idx_ar_tenant_due_date ON account_receivable(tenant_id, due_date);
CREATE INDEX idx_ar_customer ON account_receivable(customer_id);
CREATE INDEX idx_ar_sale ON account_receivable(sale_id);
