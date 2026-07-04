#!/usr/bin/env bash
# Crea el canal sentinellachannel y une el peer (ejecutar con orderer+peer en marcha).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CHANNEL="${FABRIC_CHANNEL:-sentinellachannel}"
FABRIC_TOOLS="${FABRIC_TOOLS_IMAGE:-hyperledger/fabric-tools:2.5}"

echo "==> Crear canal ${CHANNEL}"

docker run --rm \
  --network sentinella-fabric \
  -v "${ROOT}:/work" \
  -w /work \
  -e FABRIC_CFG_PATH=/work/config \
  "${FABRIC_TOOLS}" \
  bash -c "
    set -euo pipefail
    export CORE_PEER_TLS_ENABLED=true
    export CORE_PEER_LOCALMSPID=Org1MSP
    export CORE_PEER_TLS_ROOTCERT_FILE=/work/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
    export CORE_PEER_MSPCONFIGPATH=/work/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
    export CORE_PEER_ADDRESS=peer0.org1.example.com:7051

    ORDERER_CA=/work/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem
    ORDERER_TLS=/work/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/tls/server.crt

    peer channel create -o orderer.example.com:7050 -c ${CHANNEL} \
      -f /work/channel-artifacts/${CHANNEL}.tx \
      --outputBlock /work/channel-artifacts/${CHANNEL}.block \
      --tls --cafile \"\${ORDERER_CA}\" \
      --ordererTLSHostnameOverride orderer.example.com \
      --ordererIdentity \"\${ORDERER_TLS}\"

    peer channel join -b /work/channel-artifacts/${CHANNEL}.block

    peer channel update -o orderer.example.com:7050 -c ${CHANNEL} \
      -f /work/channel-artifacts/Org1MSPanchors.tx \
      --tls --cafile \"\${ORDERER_CA}\" \
      --ordererTLSHostnameOverride orderer.example.com \
      --ordererIdentity \"\${ORDERER_TLS}\"

    echo 'Canal creado y peer unido.'
  "

echo "==> Canal ${CHANNEL} listo."
