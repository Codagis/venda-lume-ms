-- Permissões para Prestadores PJ
INSERT INTO permissions (id, code, name, description, module, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'CONTRACTOR_VIEW', 'Visualizar prestadores PJ', 'Ver listagem e detalhes de prestadores de serviço PJ', 'CONTRACTORS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'CONTRACTOR_CREATE', 'Criar prestadores PJ', 'Cadastrar novos prestadores PJ', 'CONTRACTORS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'CONTRACTOR_EDIT', 'Editar prestadores PJ', 'Alterar dados de prestadores PJ', 'CONTRACTORS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'CONTRACTOR_DELETE', 'Excluir prestadores PJ', 'Remover prestadores do cadastro', 'CONTRACTORS', NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;

-- Módulo Prestadores PJ
INSERT INTO modules (id, code, name, description, icon, route, component, display_order, view_permission_code, active, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'CONTRACTORS', 'Prestadores PJ', 'Cadastro de prestadores de serviço PJ e notas fiscais', 'FileTextOutlined', '/contractors', 'Contractors', 16, 'CONTRACTOR_VIEW', true, NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;
