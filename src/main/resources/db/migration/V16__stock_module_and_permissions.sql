-- Permissões para Estoque
INSERT INTO permissions (id, code, name, description, module, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'STOCK_VIEW', 'Visualizar estoque', 'Ver gestão de estoque e movimentações', 'STOCK', NOW(), NOW(), 0),
    (gen_random_uuid(), 'STOCK_MANAGE', 'Gerenciar estoque', 'Registrar entradas, saídas e ajustes', 'STOCK', NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;

-- Módulo Estoque
INSERT INTO modules (id, code, name, description, icon, route, component, display_order, view_permission_code, active, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'STOCK', 'Estoque', 'Controle de estoque e movimentações', 'InboxOutlined', '/stock', 'Stock', 11, 'STOCK_VIEW', true, NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;
