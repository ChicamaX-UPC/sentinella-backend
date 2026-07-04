#!/usr/bin/env sh
# Genera MSP, genesis block y artefactos de canal (cryptogen + configtxgen).
set -eu
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FABRIC_TOOLS="${FABRIC_TOOLS_IMAGE:-hyperledger/fabric-tools:2.5}"
CHANNEL="${FABRIC_CHANNEL:-sentinellachannel}"

echo "==> Bootstrap Fabric (cryptogen + configtxgen)"
mkdir -p "${ROOT}/organizations" "${ROOT}/channel-artifacts" "${ROOT}/gateway-identity"

docker run --rm \
  -v "${ROOT}:/work" \
  -w /work \
  -e FABRIC_CFG_PATH=/work/config \
  "${FABRIC_TOOLS}" \
  sh -c "
    set -eu
    rm -rf organizations/*
    cryptogen generate --config=config/crypto-config.yaml --output=organizations
    configtxgen --profile SentinellaGenesis --channelID system-channel --outputBlock channel-artifacts/genesis.block
    configtxgen --profile SentinellaChannel --channelID ${CHANNEL} --outputCreateChannelTx channel-artifacts/${CHANNEL}.tx
    configtxgen --profile SentinellaChannel --channelID ${CHANNEL} --asOrg Org1MSP --outputAnchorPeersUpdate channel-artifacts/Org1MSPanchors.tx
    USER_DIR=organizations/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp
    cp \"\${USER_DIR}/signcerts/\"*.pem gateway-identity/cert.pem
    cp \"\${USER_DIR}/keystore/\"* gateway-identity/key.pem
    chmod 644 gateway-identity/cert.pem gateway-identity/key.pem
    echo 'Bootstrap completado.'
  "

echo "==> Listo. Siguiente: docker compose -f docker-compose.fabric.yml up -d"
