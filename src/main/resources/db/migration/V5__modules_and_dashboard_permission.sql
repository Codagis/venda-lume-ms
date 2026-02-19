-- Permissão para visualizar o Dashboard
INSERT INTO permissions (id, code, name, description, module, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'DASHBOARD_VIEW', 'Visualizar dashboard', 'Acessar a tela inicial', 'DASHBOARD', NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;

-- Tabela de módulos (telas/rotas dinâmicas do frontend)
CREATE TABLE modules (
    id UUID NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    icon VARCHAR(50),
    route VARCHAR(100) NOT NULL,
    component VARCHAR(80) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    view_permission_code VARCHAR(80) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by UUID,
    updated_by UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_modules PRIMARY KEY (id),
    CONSTRAINT uk_module_code UNIQUE (code)
);

CREATE INDEX idx_module_active ON modules(active);
CREATE INDEX idx_module_display_order ON modules(display_order);

COMMENT ON TABLE modules IS 'Módulos/telas do sistema. Frontend usa para montar rotas e menu dinamicamente.';

-- Seed: módulos das telas existentes
INSERT INTO modules (id, code, name, description, icon, route, component, display_order, view_permission_code, active, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'DASHBOARD', 'Dashboard', 'Tela inicial', 'DashboardOutlined', '/', 'Dashboard', 0, 'DASHBOARD_VIEW', true, NOW(), NOW(), 0),
    (gen_random_uuid(), 'PRODUCTS', 'Produtos', 'Cadastro de produtos', 'ShoppingOutlined', '/products', 'Products', 10, 'PRODUCT_VIEW', true, NOW(), NOW(), 0),
    (gen_random_uuid(), 'SETTINGS', 'Configurações', 'Empresas, perfis e permissões', 'SettingOutlined', '/settings', 'Settings', 100, 'PROFILE_VIEW', true, NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;
