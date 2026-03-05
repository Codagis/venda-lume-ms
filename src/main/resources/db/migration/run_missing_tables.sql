-- Execute este script manualmente no banco vendalume-db se as tabelas nao existirem
-- Conecte via pgAdmin, DBeaver ou: psql -h localhost -U postgres -d vendalume-db -f run_missing_tables.sql

-- 1. Tabela suppliers (se nao existir)
CREATE TABLE IF NOT EXISTS suppliers (
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
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_suppliers PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_supplier_tenant_active ON suppliers(tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_supplier_tenant_name ON suppliers(tenant_id, name);
CREATE INDEX IF NOT EXISTS idx_supplier_tenant_document ON suppliers(tenant_id, document);

-- 2. Tabela account_payable (se nao existir)
CREATE TABLE IF NOT EXISTS account_payable (
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
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_account_payable PRIMARY KEY (id),
    CONSTRAINT fk_ap_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_ap_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_ap_tenant_status ON account_payable(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_ap_tenant_due_date ON account_payable(tenant_id, due_date);
CREATE INDEX IF NOT EXISTS idx_ap_supplier ON account_payable(supplier_id);

-- 3. Tabela account_receivable (se nao existir)
CREATE TABLE IF NOT EXISTS account_receivable (
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
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_account_receivable PRIMARY KEY (id),
    CONSTRAINT fk_ar_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_ar_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    CONSTRAINT fk_ar_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_ar_tenant_status ON account_receivable(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_ar_tenant_due_date ON account_receivable(tenant_id, due_date);
CREATE INDEX IF NOT EXISTS idx_ar_customer ON account_receivable(customer_id);
CREATE INDEX IF NOT EXISTS idx_ar_sale ON account_receivable(sale_id);

-- 4. Permissoes e modulos (idempotente)
INSERT INTO permissions (id, code, name, description, module, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'SUPPLIER_VIEW', 'Visualizar fornecedores', 'Ver listagem e detalhes de fornecedores', 'SUPPLIERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'SUPPLIER_CREATE', 'Criar fornecedores', 'Cadastrar novos fornecedores', 'SUPPLIERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'SUPPLIER_UPDATE', 'Editar fornecedores', 'Alterar dados de fornecedores', 'SUPPLIERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'SUPPLIER_DELETE', 'Excluir fornecedores', 'Remover fornecedores do cadastro', 'SUPPLIERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'COST_CONTROL_VIEW', 'Visualizar controle de custos', 'Ver contas a pagar e a receber', 'COST_CONTROL', NOW(), NOW(), 0),
    (gen_random_uuid(), 'COST_CONTROL_MANAGE', 'Gerenciar controle de custos', 'Criar, editar e registrar pagamentos/recebimentos', 'COST_CONTROL', NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;

INSERT INTO modules (id, code, name, description, icon, route, component, display_order, view_permission_code, active, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'SUPPLIERS', 'Fornecedores', 'Cadastro de fornecedores', 'ShopOutlined', '/suppliers', 'Suppliers', 13, 'SUPPLIER_VIEW', true, NOW(), NOW(), 0),
    (gen_random_uuid(), 'COST_CONTROL', 'Controle de Custos', 'Contas a pagar e contas a receber', 'DollarOutlined', '/cost-control', 'CostControl', 14, 'COST_CONTROL_VIEW', true, NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;
