-- Permissões para gerenciamento de módulos
INSERT INTO permissions (id, code, name, description, module, created_at, updated_at, version) VALUES
    (gen_random_uuid(), 'MODULE_VIEW', 'Visualizar módulos', 'Ver módulos do sistema', 'MODULES', NOW(), NOW(), 0),
    (gen_random_uuid(), 'MODULE_MANAGE', 'Gerenciar módulos', 'Criar e editar módulos', 'MODULES', NOW(), NOW(), 0)
ON CONFLICT (code) DO NOTHING;
