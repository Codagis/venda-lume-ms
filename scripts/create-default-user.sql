-- Script para criar usuario padrao no PostgreSQL
-- Senha: admin123 (hash BCrypt cost 12)
-- Executar: psql -U postgres -d commo-db -f create-default-user.sql

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
) VALUES (
    gen_random_uuid(),
    'admin',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.G6qFzKzF9qFz7e',
    'admin@commo.local',
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
) ON CONFLICT (username) DO NOTHING;
