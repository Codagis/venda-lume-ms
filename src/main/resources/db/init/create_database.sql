-- Script para criar o banco de dados Commo (executar como superuser)
-- Comando: psql -U postgres -f create_database.sql
-- Se o banco já existir, ignore o erro "database already exists"

CREATE DATABASE "commo-db" WITH ENCODING 'UTF8';
