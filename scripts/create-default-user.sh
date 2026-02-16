#!/bin/bash
# Cria usuario padrao via API
# Requer: API rodando (padrao http://localhost:8080)
# Uso: ./create-default-user.sh

API_URL="${COMMO_API_URL:-http://localhost:8080/api}"

response=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@commo.local",
    "fullName": "Administrador",
    "role": "SUPER_ADMIN"
  }')

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" -eq 201 ]; then
  echo "Usuario criado com sucesso:"
  echo "  Usuario: admin"
  echo "  Senha: admin123"
  echo "  Email: admin@commo.local"
  exit 0
elif [ "$http_code" -eq 400 ]; then
  if echo "$body" | grep -q "já está em uso\|já existente"; then
    echo "Usuario 'admin' ja existe. Nenhuma acao necessaria."
    exit 0
  fi
fi

echo "Erro ao criar usuario (HTTP $http_code): $body"
exit 1
