# Hyperledger Fabric — Sentinella (US17 / RF-17 / C-11)

Red permissioned con **cryptogen**, canal `sentinellachannel` y chaincode `sentinella-ledger`.

## Arranque completo (primera vez)

### Windows (PowerShell + Docker + Git Bash)

```powershell
cd sentinella-backend/infra/hyperledger-fabric
.\scripts\fabric-up.ps1
```

### Linux / macOS

```bash
cd sentinella-backend/infra/hyperledger-fabric
chmod +x scripts/*.sh
./scripts/fabric-up.sh
```

Esto ejecuta: `bootstrap-network` → orderer + peer → `create-channel` → `deploy-chaincode`.

## Activar anclaje real en Sentinella

Stack unificado (Fabric + backend en **un solo grupo** Docker Desktop):

```bash
cd sentinella-backend
docker compose -f docker-compose.yml -f docker-compose.fabric.yml -f docker-compose.fabric-stack.yml --profile fabric up -d
```

O en `.env`: `COMPOSE_PROFILES=fabric` y luego `docker compose -f docker-compose.yml -f docker-compose.fabric.yml -f docker-compose.fabric-stack.yml up -d`.

O exportar en `.env`:

```env
BLOCKCHAIN_FABRIC_ENABLED=true
```

## Producción

| Componente | Desarrollo (este repo) | Producción recomendada |
|------------|------------------------|-------------------------|
| Identidades | cryptogen / User1 | Fabric CA + HSM |
| Red | 1 orderer, 1 peer | 3+ orderers, 2+ orgs, HA |
| Despliegue | Docker Compose | K8s + Fabric Operator |
| Secretos | Volúmenes locales | Vault / K8s Secrets |

Variables obligatorias en prod (`docker-compose.prod.yml`):

- `BLOCKCHAIN_FABRIC_ENABLED=true`
- Montajes de `organizations/`, `gateway-identity/`, `config/`
- Red `sentinella-fabric` en el mismo proyecto Compose (`docker-compose.fabric.yml` en la raíz del backend)

## Verificación

```bash
# Health
curl http://localhost:8093/v1/blockchain/health

# Ledger por alerta (con JWT)
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/blockchain/ledger?entityId={alertId}"

# Verificar tx
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/blockchain/ledger/verify/{recordId}"
```

## Reintentos (RNF-16)

- Fabric Gateway: 3 reintentos con backoff exponencial (`BLOCKCHAIN_FABRIC_RETRY_*`)
- DLQ `blockchain.register.dlq`: hasta 5 republicaciones (`BLOCKCHAIN_DLQ_MAX_REPUBLISH`)

## Sin Fabric (stub)

`BLOCKCHAIN_FABRIC_ENABLED=false` → índice PostgreSQL con `fabricTxId` stub. Útil para desarrollo sin Docker Fabric.

Ver [`docs/blockchain/BLOCKCHAIN_LIFECYCLE_GAPS.md`](../../docs/blockchain/BLOCKCHAIN_LIFECYCLE_GAPS.md).
