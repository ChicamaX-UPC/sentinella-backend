#!/usr/bin/env bash
# Genera material criptografico de desarrollo para Fabric (requiere Docker).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CRYPTO="${ROOT}/crypto"

echo "Generando crypto de desarrollo en ${CRYPTO} ..."

docker run --rm -v "${ROOT}:/work" hyperledger/fabric-tools:2.5 sh -c '
set -e
mkdir -p /work/crypto/peer/tls /work/crypto/user /work/crypto/orderer/msp
openssl req -x509 -newkey rsa:2048 -keyout /work/crypto/peer/tls/server.key -out /work/crypto/peer/tls/server.crt -days 365 -nodes -subj "/CN=peer0.org1.example.com"
cp /work/crypto/peer/tls/server.crt /work/crypto/peer/tls/ca.crt
cp /work/crypto/peer/tls/server.crt /work/crypto/user/cert.pem
cp /work/crypto/peer/tls/server.key /work/crypto/user/key.pem
echo "Crypto de desarrollo generado."
'

echo "Listo. Levantar red: docker compose -f docker-compose.fabric.yml up -d"
