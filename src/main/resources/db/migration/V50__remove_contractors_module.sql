-- Remove módulo e permissões de Prestadores PJ (funcionalidade extinta)

-- Remove vínculos perfil-permissão
DELETE FROM profile_permissions
WHERE permission_id IN (
    SELECT id FROM permissions WHERE module = 'CONTRACTORS' OR code LIKE 'CONTRACTOR_%'
);

-- Remove permissões
DELETE FROM permissions
WHERE module = 'CONTRACTORS' OR code LIKE 'CONTRACTOR_%';

-- Remove módulo
DELETE FROM modules
WHERE code = 'CONTRACTORS';

