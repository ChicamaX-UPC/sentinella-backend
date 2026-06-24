# Arranque local de Stripe + recordatorio de backend
$ErrorActionPreference = "Stop"
$backendRoot = Split-Path $PSScriptRoot -Parent

Write-Host "=== Sentinella · Stripe dev ===" -ForegroundColor Cyan
Write-Host "1. Asegúrate de que Docker Desktop esté corriendo."
Write-Host "2. En otra terminal: cd sentinella-backend && docker compose up -d"
Write-Host "3. Frontend: cd sentinella-frontend && npm run dev"
Write-Host ""

$envPath = Join-Path $backendRoot ".env"
if (-not (Test-Path $envPath)) {
    Write-Host "Falta .env con Stripe. Ejecuta: .\scripts\stripe-setup.ps1" -ForegroundColor Yellow
    exit 1
}

$hasPrices = Select-String -Path $envPath -Pattern '^STRIPE_PRICE_ECONOMY_RECURRING=price_' -Quiet
if (-not $hasPrices) {
    Write-Host "Precios Stripe no configurados. Ejecuta: .\scripts\stripe-setup.ps1" -ForegroundColor Yellow
    exit 1
}

$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
$forwardUrl = "http://localhost:8080/api/v1/payments/webhooks/stripe"

Write-Host "Webhook -> $forwardUrl" -ForegroundColor Green
Write-Host "Presiona Ctrl+C para detener stripe listen." -ForegroundColor Gray
& "$PSScriptRoot\stripe-listen.ps1"
