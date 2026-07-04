# Arranque completo de red Fabric (Windows + Docker)
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
$FabricTools = if ($env:FABRIC_TOOLS_IMAGE) { $env:FABRIC_TOOLS_IMAGE } else { "hyperledger/fabric-tools:2.5" }
$Channel = if ($env:FABRIC_CHANNEL) { $env:FABRIC_CHANNEL } else { "sentinellachannel" }
$CcName = if ($env:FABRIC_CHAINCODE) { $env:FABRIC_CHAINCODE } else { "sentinella-ledger" }

function Invoke-FabricSh([string]$Script) {
    docker run --rm -v "${Root}:/work" -w /work -e FABRIC_CFG_PATH=/work/config $FabricTools sh -c $Script
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

Write-Host "=== Sentinella Fabric - arranque completo ==="

Write-Host "==> Bootstrap (cryptogen + configtxgen)"
New-Item -ItemType Directory -Force -Path organizations, channel-artifacts, gateway-identity | Out-Null

Invoke-FabricSh "rm -rf organizations/* && cryptogen generate --config=config/crypto-config.yaml --output=organizations"
Invoke-FabricSh "configtxgen --profile SentinellaGenesis --channelID system-channel --outputBlock channel-artifacts/genesis.block"
Invoke-FabricSh "configtxgen --profile SentinellaChannel --channelID $Channel --outputCreateChannelTx channel-artifacts/${Channel}.tx"
Invoke-FabricSh "configtxgen --profile SentinellaChannel --channelID $Channel --asOrg Org1MSP --outputAnchorPeersUpdate channel-artifacts/Org1MSPanchors.tx"
Invoke-FabricSh 'USER_DIR=organizations/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp && cp ${USER_DIR}/signcerts/*.pem gateway-identity/cert.pem && cp ${USER_DIR}/keystore/* gateway-identity/key.pem && chmod 644 gateway-identity/cert.pem gateway-identity/key.pem'

Write-Host "==> Levantar orderer y peer (proyecto sentinella-backend unificado)"
$BackendRoot = Split-Path -Parent $Root
# Migración: contenedores Fabric del compose independiente (hyperledger-fab)
docker rm -f sentinella-fabric-orderer sentinella-fabric-peer0 2>$null | Out-Null
Push-Location $BackendRoot
docker compose -f docker-compose.yml -f docker-compose.fabric.yml --profile fabric up -d fabric-orderer fabric-peer0
$composeExit = $LASTEXITCODE
Pop-Location
if ($composeExit -ne 0) { exit $composeExit }
Write-Host "Esperando orderer y peer..."
Start-Sleep -Seconds 15

Write-Host "==> Crear canal $Channel"
docker run --rm --network sentinella-fabric -v "${Root}:/work" -w /work $FabricTools sh -c @"
set -e
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID=Org1MSP
export CORE_PEER_TLS_ROOTCERT_FILE=/work/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/work/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=peer0.org1.example.com:7051
ORDERER_CA=/work/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
ORDERER_TLS=/work/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/tls/server.crt
peer channel create -o orderer.example.com:7050 -c $Channel -f /work/channel-artifacts/${Channel}.tx --outputBlock /work/channel-artifacts/${Channel}.block --tls --cafile `"`${ORDERER_CA}`" --ordererTLSHostnameOverride orderer.example.com
peer channel join -b /work/channel-artifacts/${Channel}.block
peer channel update -o orderer.example.com:7050 -c $Channel -f /work/channel-artifacts/Org1MSPanchors.tx --tls --cafile `"`${ORDERER_CA}`" --ordererTLSHostnameOverride orderer.example.com
"@
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "==> Desplegar chaincode $CcName"
# go.sum requerido para empaquetar chaincode golang
docker run --rm -v "${Root}:/work" -w /work/chaincode/${CcName} golang:1.21 sh -c "go mod tidy"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

docker run --rm --network sentinella-fabric -v "${Root}:/work" -v /var/run/docker.sock:/var/run/docker.sock -w /work $FabricTools sh -c @"
set -e
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID=Org1MSP
export CORE_PEER_TLS_ROOTCERT_FILE=/work/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/work/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=peer0.org1.example.com:7051
ORDERER_CA=/work/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
peer lifecycle chaincode package /work/${CcName}.tar.gz --path /work/chaincode/${CcName} --lang golang --label ${CcName}_1.0
peer lifecycle chaincode install /work/${CcName}.tar.gz
PACKAGE_ID=`$(peer lifecycle chaincode calculatepackageid /work/${CcName}.tar.gz)
peer lifecycle chaincode approveformyorg -o orderer.example.com:7050 --channelID $Channel --name $CcName --version 1.0 --package-id `"`${PACKAGE_ID}`" --sequence 1 --tls --cafile `"`${ORDERER_CA}`" --ordererTLSHostnameOverride orderer.example.com
peer lifecycle chaincode commit -o orderer.example.com:7050 --channelID $Channel --name $CcName --version 1.0 --sequence 1 --tls --cafile `"`${ORDERER_CA}`" --ordererTLSHostnameOverride orderer.example.com --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles /work/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
peer lifecycle chaincode querycommitted --channelID $Channel --name $CcName
"@
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host ""
Write-Host "=== Red Fabric lista ==="
docker ps --filter name=sentinella-fabric --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
Write-Host ""
Write-Host "Siguiente paso:"
Write-Host "  cd $BackendRoot"
Write-Host "  docker compose -f docker-compose.yml -f docker-compose.fabric.yml -f docker-compose.fabric-stack.yml --profile fabric up -d"
