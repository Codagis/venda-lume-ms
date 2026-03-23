INSERT INTO permissions (id, code, name, description, module, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'EMPLOYEE_VIEW', 'Visualizar funcionários', 'Ver listagem e detalhes de funcionários', 'EMPLOYEES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'EMPLOYEE_CREATE', 'Criar funcionários', 'Cadastrar novos funcionários', 'EMPLOYEES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'EMPLOYEE_EDIT', 'Editar funcionários', 'Alterar dados de funcionários', 'EMPLOYEES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'EMPLOYEE_DELETE', 'Excluir funcionários', 'Remover funcionários', 'EMPLOYEES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'PAYROLL_VIEW', 'Ver folha de pagamento', 'Consultar e exportar folha de pagamento', 'EMPLOYEES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'PAYROLL_GENERATE', 'Gerar folha do mês', 'Gerar contas a pagar dos funcionários', 'EMPLOYEES', NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;

INSERT INTO modules (id, code, name, description, icon, route, component, display_order, view_permission_code, active, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'EMPLOYEES', 'Funcionários', 'Cadastro de funcionários e folha de pagamento', 'TeamOutlined', '/employees', 'Employees', 15, 'EMPLOYEE_VIEW', true, NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;
