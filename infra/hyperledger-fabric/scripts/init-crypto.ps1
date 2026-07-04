# Redirige al bootstrap real (cryptogen + configtxgen)
$Root = Split-Path -Parent $PSScriptRoot
bash "$Root/scripts/bootstrap-network.sh"
