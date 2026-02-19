-- Usuario padrao do sistema: admin / admin123
-- Usa pgcrypto para gerar hash BCrypt compativel com Spring Security

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users (
    id,
    username,
    password_hash,
    email,
    full_name,
    active,
    email_verified,
    phone_verified,
    role,
    timezone,
    locale,
    failed_login_attempts,
    two_factor_enabled,
    refresh_token_version,
    created_at,
    updated_at,
    version
)
SELECT
    gen_random_uuid(),
    'admin',
    crypt('admin123', gen_salt('bf', 12)),
    'admin@vendalume.local',
    'Administrador',
    true,
    false,
    false,
    'SUPER_ADMIN',
    'America/Sao_Paulo',
    'pt_BR',
    0,
    false,
    0,
    NOW(),
    NOW(),
    0
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');
