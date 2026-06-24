# Provisiona productos/precios Sentinella en Stripe (modo test) y actualiza sentinella-backend/.env
$ErrorActionPreference = "Stop"

$backendRoot = Split-Path $PSScriptRoot -Parent
$envFile = Join-Path $backendRoot ".env"
$configToml = Join-Path $env:USERPROFILE ".config\stripe\config.toml"

if (-not (Test-Path $configToml)) {
    throw "Stripe CLI no autenticado. Ejecuta: stripe login"
}

function Invoke-StripeJson {
    param([string[]]$StripeArgs)
    $raw = (& stripe @StripeArgs 2>&1) -join "`n"
    if ($LASTEXITCODE -ne 0) { throw $raw }
    $start = $raw.IndexOf('{')
    if ($start -lt 0) { throw "No JSON en salida de stripe $($StripeArgs -join ' '): $raw" }
    return ($raw.Substring($start)) | ConvertFrom-Json
}

function New-Plan {
    param(
        [string]$Name,
        [string]$Code,
        [int]$RecurringCents,
        [int]$SetupCents
    )
    Write-Host "Creando plan $Code..."
    $product = Invoke-StripeJson @(
        "products", "create",
        "-d", "name=Sentinella $Name",
        "-d", "metadata[plan_code]=$Code",
        "-d", "metadata[sensor_limit]=$(
            switch ($Code) { 'ECONOMY' {5} 'PREMIUM' {12} 'MAX' {20} default {5} }
        )"
    )
    $recurring = Invoke-StripeJson @(
        "prices", "create",
        "-d", "product=$($product.id)",
        "-d", "unit_amount=$RecurringCents",
        "-d", "currency=usd",
        "-d", "recurring[interval]=month",
        "-d", "metadata[plan_code]=$Code",
        "-d", "metadata[price_type]=recurring"
    )
    $setup = Invoke-StripeJson @(
        "prices", "create",
        "-d", "product=$($product.id)",
        "-d", "unit_amount=$SetupCents",
        "-d", "currency=usd",
        "-d", "metadata[plan_code]=$Code",
        "-d", "metadata[price_type]=setup"
    )
    return [ordered]@{
        Code             = $Code
        ProductId        = $product.id
        RecurringPriceId = $recurring.id
        SetupPriceId     = $setup.id
    }
}

Write-Host "Configurando Billing Portal..."
try {
    $existingPortal = Invoke-StripeJson @("billing_portal", "configurations", "list", "--limit", "1")
    if ($existingPortal.data.Count -eq 0) {
        Invoke-StripeJson @(
            "billing_portal", "configurations", "create",
            "-d", "business_profile[headline]=Sentinella",
            "-d", "features[subscription_cancel][enabled]=true",
            "-d", "features[payment_method_update][enabled]=true",
            "-d", "features[invoice_history][enabled]=true"
        ) | Out-Null
        Write-Host "Billing Portal creado."
    } else {
        Write-Host "Billing Portal ya existe."
    }
} catch {
    Write-Warning "No se pudo configurar Billing Portal: $_"
}

$economy = New-Plan -Name "Economy" -Code "ECONOMY" -RecurringCents 9000 -SetupCents 10000
$premium = New-Plan -Name "Premium" -Code "PREMIUM" -RecurringCents 14000 -SetupCents 24000
$max = New-Plan -Name "Max" -Code "MAX" -RecurringCents 22000 -SetupCents 40000

$skTest = (Select-String -Path $configToml -Pattern '^test_mode_api_key\s*=\s*''(.+)''' | ForEach-Object { $_.Matches[0].Groups[1].Value })
if (-not $skTest) { throw "No se encontró test_mode_api_key en $configToml" }

$stripeBlock = @"

# --- Stripe (generado por scripts/stripe-setup.ps1) ---
STRIPE_SECRET_KEY=$skTest
# STRIPE_WEBHOOK_SECRET lo define stripe listen (scripts/stripe-listen.ps1)
STRIPE_SUCCESS_URL=http://localhost:3000/dashboard?billing=success
STRIPE_CANCEL_URL=http://localhost:3000/profile?billing=cancel
STRIPE_PORTAL_RETURN_URL=http://localhost:3000/profile?billing=1
STRIPE_DEMO_CONFIRM_ENABLED=true
STRIPE_PRICE_ECONOMY_RECURRING=$($economy.RecurringPriceId)
STRIPE_PRICE_ECONOMY_SETUP=$($economy.SetupPriceId)
STRIPE_PRICE_PREMIUM_RECURRING=$($premium.RecurringPriceId)
STRIPE_PRICE_PREMIUM_SETUP=$($premium.SetupPriceId)
STRIPE_PRICE_MAX_RECURRING=$($max.RecurringPriceId)
STRIPE_PRICE_MAX_SETUP=$($max.SetupPriceId)
"@

if (Test-Path $envFile) {
    $content = Get-Content $envFile -Raw
    if ($content -match '(?ms)^# --- Stripe.*?(?=^# |\z)') {
        $content = $content -replace '(?ms)^# --- Stripe.*?(?=^# |\z)', ''
    }
    $content = $content.TrimEnd() + "`r`n" + $stripeBlock + "`r`n"
} else {
    $content = $stripeBlock + "`r`n"
}
Set-Content -Path $envFile -Value $content -Encoding UTF8

Write-Host ""
Write-Host "Listo. Variables Stripe escritas en $envFile"

$whsecOutput = & stripe listen --print-secret 2>&1
$whsec = ($whsecOutput | Select-String -Pattern 'whsec_\S+' | ForEach-Object { $_.Matches[0].Value }) | Select-Object -First 1
if ($whsec) {
    $content = Get-Content $envFile -Raw
    if ($content -match '(?m)^STRIPE_WEBHOOK_SECRET=.*$') {
        $content = $content -replace '(?m)^STRIPE_WEBHOOK_SECRET=.*$', "STRIPE_WEBHOOK_SECRET=$whsec"
    } else {
        $content = $content -replace '(?m)^(STRIPE_SECRET_KEY=.*)$', "`$1`r`nSTRIPE_WEBHOOK_SECRET=$whsec"
    }
    Set-Content -Path $envFile -Value $content.TrimEnd() + "`r`n" -Encoding UTF8
    Write-Host "STRIPE_WEBHOOK_SECRET actualizado."
}

Write-Host "Economy: $($economy.RecurringPriceId) + $($economy.SetupPriceId)"
Write-Host "Premium: $($premium.RecurringPriceId) + $($premium.SetupPriceId)"
Write-Host "Max:     $($max.RecurringPriceId) + $($max.SetupPriceId)"
Write-Host ""
Write-Host "Siguiente: .\scripts\stripe-listen.ps1 (webhook local)"
