#!/usr/bin/env bash
# Arranque completo: bootstrap -> red -> canal -> chaincode
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "${ROOT}"

echo "=== Sentinella Fabric — arranque completo ==="
bash scripts/bootstrap-network.sh
docker compose -f docker-compose.fabric.yml up -d fabric-orderer fabric-peer0
echo "Esperando orderer y peer..."
sleep 8
bash scripts/create-channel.sh
bash scripts/deploy-chaincode.sh
echo ""
echo "=== Red Fabric lista ==="
echo "  Peer:    peer0.org1.example.com:7051"
echo "  Canal:   sentinellachannel"
echo "  Chaincode: sentinella-ledger"
echo ""
echo "Activar en Sentinella:"
echo "  BLOCKCHAIN_FABRIC_ENABLED=true"
echo "  docker compose -f ../../docker-compose.yml -f ../../docker-compose.fabric-stack.yml up -d"
