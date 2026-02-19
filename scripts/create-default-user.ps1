# Cria usuario padrao via API
# Requer: API rodando em http://localhost:8080
# Uso: .\create-default-user.ps1

$apiUrl = $env:VENDALUME_API_URL ?? "http://localhost:8080/api"
$body = @{
    username = "admin"
    password = "admin123"
    email = "admin@vendalume.local"
    fullName = "Administrador"
    role = "SUPER_ADMIN"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$apiUrl/auth/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body

    Write-Host "Usuario criado com sucesso:" -ForegroundColor Green
    Write-Host "  Usuario: admin"
    Write-Host "  Senha: admin123"
    Write-Host "  Email: admin@vendalume.local"
}
catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $errBody = $reader.ReadToEnd() | ConvertFrom-Json
        if ($errBody.message -match "já está em uso|já existente") {
            Write-Host "Usuario 'admin' ja existe. Nenhuma acao necessaria." -ForegroundColor Yellow
            exit 0
        }
    }
    Write-Host "Erro: $_" -ForegroundColor Red
    exit 1
}
