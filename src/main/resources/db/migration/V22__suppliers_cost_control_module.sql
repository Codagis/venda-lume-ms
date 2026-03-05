-- Permissões para fornecedores
INSERT INTO permissions (id, code, name, description, module, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'SUPPLIER_VIEW', 'Visualizar fornecedores', 'Ver listagem e detalhes de fornecedores', 'SUPPLIERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'SUPPLIER_CREATE', 'Criar fornecedores', 'Cadastrar novos fornecedores', 'SUPPLIERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'SUPPLIER_UPDATE', 'Editar fornecedores', 'Alterar dados de fornecedores', 'SUPPLIERS', NOW(), NOW(), 0),
    (gen_random_uuid(), 'SUPPLIER_DELETE', 'Excluir fornecedores', 'Remover fornecedores do cadastro', 'SUPPLIERS', NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;

-- Permissões para controle de custos
INSERT INTO permissions (id, code, name, description, module, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'COST_CONTROL_VIEW', 'Visualizar controle de custos', 'Ver contas a pagar e a receber', 'COST_CONTROL', NOW(), NOW(), 0),
    (gen_random_uuid(), 'COST_CONTROL_MANAGE', 'Gerenciar controle de custos', 'Criar, editar e registrar pagamentos/recebimentos', 'COST_CONTROL', NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;

-- Módulo Fornecedores
INSERT INTO modules (id, code, name, description, icon, route, component, display_order, view_permission_code, active, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'SUPPLIERS', 'Fornecedores', 'Cadastro de fornecedores', 'ShopOutlined', '/suppliers', 'Suppliers', 13, 'SUPPLIER_VIEW', true, NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;

-- Módulo Controle de Custos
INSERT INTO modules (id, code, name, description, icon, route, component, display_order, view_permission_code, active, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'COST_CONTROL', 'Controle de Custos', 'Contas a pagar e contas a receber', 'DollarOutlined', '/cost-control', 'CostControl', 14, 'COST_CONTROL_VIEW', true, NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;
