#!/usr/bin/env bash
# Empaqueta, instala y despliega chaincode sentinella-ledger (Fabric 2.x lifecycle).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CHANNEL="${FABRIC_CHANNEL:-sentinellachannel}"
CC_NAME="${FABRIC_CHAINCODE:-sentinella-ledger}"
CC_VERSION="${FABRIC_CHAINCODE_VERSION:-1.0}"
CC_SEQUENCE="${FABRIC_CHAINCODE_SEQUENCE:-1}"
CC_LABEL="${CC_NAME}_${CC_VERSION}"
FABRIC_TOOLS="${FABRIC_TOOLS_IMAGE:-hyperledger/fabric-tools:2.5}"

echo "==> Desplegar chaincode ${CC_NAME} v${CC_VERSION} en ${CHANNEL}"

docker run --rm \
  --network sentinella-fabric \
  -v "${ROOT}:/work" \
  -v /var/run/docker.sock:/var/run/docker.sock \
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

    peer lifecycle chaincode package /work/${CC_NAME}.tar.gz \
      --path /work/chaincode/${CC_NAME} \
      --lang golang \
      --label ${CC_LABEL}

    peer lifecycle chaincode install /work/${CC_NAME}.tar.gz

    PACKAGE_ID=\$(peer lifecycle chaincode calculatepackageid /work/${CC_NAME}.tar.gz)
    echo \"Package ID: \${PACKAGE_ID}\"

    peer lifecycle chaincode approveformyorg \
      -o orderer.example.com:7050 \
      --channelID ${CHANNEL} \
      --name ${CC_NAME} \
      --version ${CC_VERSION} \
      --package-id \"\${PACKAGE_ID}\" \
      --sequence ${CC_SEQUENCE} \
      --tls --cafile \"\${ORDERER_CA}\" \
      --ordererTLSHostnameOverride orderer.example.com \
      --ordererIdentity \"\${ORDERER_TLS}\"

    peer lifecycle chaincode commit \
      -o orderer.example.com:7050 \
      --channelID ${CHANNEL} \
      --name ${CC_NAME} \
      --version ${CC_VERSION} \
      --sequence ${CC_SEQUENCE} \
      --tls --cafile \"\${ORDERER_CA}\" \
      --ordererTLSHostnameOverride orderer.example.com \
      --ordererIdentity \"\${ORDERER_TLS}\" \
      --peerAddresses peer0.org1.example.com:7051 \
      --tlsRootCertFiles /work/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt

    peer lifecycle chaincode querycommitted --channelID ${CHANNEL} --name ${CC_NAME}
    echo 'Chaincode desplegado.'
  "

echo "==> Chaincode ${CC_NAME} desplegado."
