-- Garante permissões para todas as telas atuais (caso não existam)
INSERT INTO permissions (id, code, name, description, module, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'DASHBOARD_VIEW', 'Visualizar dashboard', 'Acessar a tela inicial', 'DASHBOARD', NOW(), NOW(), 0),
    (gen_random_uuid(), 'PRODUCT_VIEW', 'Visualizar produtos', 'Ver listagem e detalhes de produtos', 'PRODUCTS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'MODULE_VIEW', 'Visualizar módulos', 'Ver módulos do sistema', 'MODULES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'MODULE_MANAGE', 'Gerenciar módulos', 'Criar e editar módulos', 'MODULES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'USER_VIEW', 'Visualizar usuários', 'Ver usuários da empresa', 'USERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'USER_CREATE', 'Criar usuários', 'Cadastrar usuários', 'USERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'PROFILE_VIEW', 'Visualizar perfis', 'Ver perfis e configurações', 'PROFILES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'PROFILE_MANAGE', 'Gerenciar perfis', 'Criar e editar perfis', 'PROFILES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'TENANT_VIEW', 'Visualizar empresas', 'Ver empresas (somente root)', 'TENANTS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'TENANT_MANAGE', 'Gerenciar empresas', 'Criar e editar empresas (somente root)', 'TENANTS', NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;
