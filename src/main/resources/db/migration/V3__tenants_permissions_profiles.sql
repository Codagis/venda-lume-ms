-- VendaLume - Empresas (tenants), Permissões e Perfis
-- Multi-tenancy: Codagis é a dona do SaaS; empresas compram e têm dados isolados.
-- Usuário root (is_root = true) tem acesso a tudo.

-- Tabela: tenants (empresas clientes do SaaS)
CREATE TABLE tenants (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255),
    document VARCHAR(20),
    email VARCHAR(255),
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_tenants PRIMARY KEY (id)
);

CREATE INDEX idx_tenant_active ON tenants(active);
CREATE INDEX idx_tenant_document ON tenants(document);
CREATE UNIQUE INDEX uk_tenant_document ON tenants(document) WHERE document IS NOT NULL AND document != '';

COMMENT ON TABLE tenants IS 'Empresas clientes do SaaS VendaLume (multi-tenancy)';

-- Tabela: permissions (permissões granulares do sistema)
CREATE TABLE permissions (
    id UUID NOT NULL,
    code VARCHAR(80) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    module VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uk_permission_code UNIQUE (code)
);

CREATE INDEX idx_permission_module ON permissions(module);

COMMENT ON TABLE permissions IS 'Permissões granulares para controle de acesso';

-- Tabela: profiles (perfis de acesso por tenant ou sistema)
CREATE TABLE profiles (
    id UUID NOT NULL,
    tenant_id UUID,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_profiles PRIMARY KEY (id),
    CONSTRAINT fk_profile_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE INDEX idx_profile_tenant ON profiles(tenant_id);
CREATE UNIQUE INDEX uk_profile_tenant_name ON profiles(tenant_id, name);

COMMENT ON TABLE profiles IS 'Perfis de acesso (conjunto de permissões). tenant_id NULL = perfil do sistema';

-- Tabela: profile_permissions (perfis x permissões)
CREATE TABLE profile_permissions (
    profile_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    CONSTRAINT pk_profile_permissions PRIMARY KEY (profile_id, permission_id),
    CONSTRAINT fk_pp_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_pp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

COMMENT ON TABLE profile_permissions IS 'Permissões atribuídas a cada perfil';

-- Colunas em users: is_root e profile_id
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_root BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_id UUID;
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_profile') THEN
    ALTER TABLE users ADD CONSTRAINT fk_user_profile FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE SET NULL;
  END IF;
END $$;
CREATE INDEX IF NOT EXISTS idx_user_profile ON users(profile_id);
COMMENT ON COLUMN users.is_root IS 'Usuário root (Codagis): acesso total a todas as empresas e funcionalidades';
COMMENT ON COLUMN users.profile_id IS 'Perfil de acesso do usuário (permissões granulares). Se null, usa role';

-- Seed: permissões iniciais do sistema
INSERT INTO permissions (id, code, name, description, module, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'PRODUCT_VIEW', 'Visualizar produtos', 'Ver listagem e detalhes de produtos', 'PRODUCTS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'PRODUCT_CREATE', 'Criar produtos', 'Cadastrar novos produtos', 'PRODUCTS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'PRODUCT_UPDATE', 'Editar produtos', 'Alterar dados de produtos', 'PRODUCTS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'PRODUCT_DELETE', 'Excluir produtos', 'Remover produtos do cadastro', 'PRODUCTS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'SALE_VIEW', 'Visualizar vendas', 'Ver vendas e relatórios', 'SALES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'SALE_CREATE', 'Registrar vendas', 'Realizar vendas no PDV', 'SALES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'SALE_CANCEL', 'Cancelar vendas', 'Cancelar vendas', 'SALES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'DELIVERY_VIEW', 'Visualizar entregas', 'Ver entregas', 'DELIVERY', NOW(), NOW(), 0),
    (gen_random_uuid(), 'DELIVERY_MANAGE', 'Gerenciar entregas', 'Alterar status e atribuir entregas', 'DELIVERY', NOW(), NOW(), 0),
    (gen_random_uuid(), 'USER_VIEW', 'Visualizar usuários', 'Ver usuários da empresa', 'USERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'USER_CREATE', 'Criar usuários', 'Cadastrar usuários', 'USERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'USER_UPDATE', 'Editar usuários', 'Alterar dados de usuários', 'USERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'USER_DELETE', 'Excluir usuários', 'Desativar ou remover usuários', 'USERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'PROFILE_VIEW', 'Visualizar perfis', 'Ver perfis de acesso', 'PROFILES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'PROFILE_MANAGE', 'Gerenciar perfis', 'Criar e editar perfis e permissões', 'PROFILES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'TENANT_VIEW', 'Visualizar empresas', 'Ver empresas (somente root)', 'TENANTS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'TENANT_MANAGE', 'Gerenciar empresas', 'Criar e editar empresas (somente root)', 'TENANTS', NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;
