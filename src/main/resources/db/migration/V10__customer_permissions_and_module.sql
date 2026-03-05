-- Permissões para cadastro de clientes
INSERT INTO permissions (id, code, name, description, module, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'CUSTOMER_VIEW', 'Visualizar clientes', 'Ver listagem e detalhes de clientes', 'CUSTOMERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'CUSTOMER_CREATE', 'Criar clientes', 'Cadastrar novos clientes', 'CUSTOMERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'CUSTOMER_UPDATE', 'Editar clientes', 'Alterar dados de clientes', 'CUSTOMERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'CUSTOMER_DELETE', 'Excluir clientes', 'Remover clientes do cadastro', 'CUSTOMERS', NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;

-- Módulo Clientes
INSERT INTO modules (id, code, name, description, icon, route, component, display_order, view_permission_code, active, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'CUSTOMERS', 'Clientes', 'Cadastro de clientes', 'UserOutlined', '/customers', 'Customers', 12, 'CUSTOMER_VIEW', true, NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;
