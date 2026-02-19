-- Empresa padrão Codagis (dona do SaaS) e marca admin como root
INSERT INTO tenants (id, name, trade_name, active, created_at, updated_at, version)
SELECT gen_random_uuid(), 'Codagis', 'Codagis', true, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM tenants WHERE name = 'Codagis');

UPDATE users SET is_root = true, tenant_id = (SELECT id FROM tenants WHERE name = 'Codagis' LIMIT 1)
WHERE username = 'admin';
