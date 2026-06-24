# Reenvía webhooks Stripe al API Gateway local y actualiza STRIPE_WEBHOOK_SECRET en .env
$ErrorActionPreference = "Stop"

$backendRoot = Split-Path $PSScriptRoot -Parent
$envFile = Join-Path $backendRoot ".env"
$forwardUrl = "http://localhost:8080/api/v1/payments/webhooks/stripe"

Write-Host "Obteniendo webhook secret..."
$secretOutput = & stripe listen --print-secret 2>&1
if ($LASTEXITCODE -ne 0) { throw ($secretOutput -join "`n") }
$whsec = ($secretOutput | Select-String -Pattern 'whsec_\S+' | ForEach-Object { $_.Matches[0].Value }) | Select-Object -First 1
if (-not $whsec) { throw "No se pudo leer whsec_ del CLI" }

if (Test-Path $envFile) {
    $content = Get-Content $envFile -Raw
    if ($content -match '(?m)^STRIPE_WEBHOOK_SECRET=.*$') {
        $content = $content -replace '(?m)^STRIPE_WEBHOOK_SECRET=.*$', "STRIPE_WEBHOOK_SECRET=$whsec"
    } else {
        $content = $content.TrimEnd() + "`r`nSTRIPE_WEBHOOK_SECRET=$whsec`r`n"
    }
    Set-Content -Path $envFile -Value $content -Encoding UTF8
    Write-Host "STRIPE_WEBHOOK_SECRET actualizado en .env"
}

Write-Host "Escuchando webhooks -> $forwardUrl"
Write-Host "Reinicia payments-service si ya estaba corriendo para cargar el nuevo secret."
& stripe listen --forward-to $forwardUrl
