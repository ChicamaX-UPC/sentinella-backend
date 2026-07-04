# Blockchain y ciclo de vida de alertas — gaps y alineación

Documento de referencia para US17 / RF-17 / C-11.

## Ciclo de vida canónico

```
RECEIVED → ACKNOWLEDGED → COMPLETED → CLOSED
                ↑
         ESCALATED (sin cambio de estado)
```

## Matriz de alineación (completa)

| Evento | entityType | relatedEntityId |
|--------|------------|-----------------|
| Alerta creada | `ALERT` | alertId |
| Reconocimiento | `ALERT_ACK` | alertId |
| Gestión completada | `ALERT_COMPLETED` | alertId |
| Cierre | `ALERT_CLOSED` | alertId |
| Escalación | `ALERT_ESCALATED` | alertId |
| Evidencia | `ALERT_EVIDENCE` | alertId (entityId = evidenceId) |
| Sensor crítico | `SENSOR_CRITICAL` | readingId |
| Ronda | `ROUND_SYNC` | roundId |
| Informe regulatorio OEFA | `REGULATORY_REPORT` | reportId |

Consulta por alerta: `GET /blockchain/ledger?entityId={alertId}` incluye evidencias vía `related_entity_id`.

## Cola offline móvil (ACK)

- IndexedDB `blockchainAckQueue` (Dexie v4) en `/mobile/*`
- Al reconocer alerta sin red: calcula hash canónico `ALERT_ACK` + encola PATCH con `clientAcknowledgedAt`
- `flushMobileSync()` envía mutaciones y verifica anclaje en `GET /blockchain/ledger`
- Backend acepta `clientAcknowledgedAt` en `PATCH /alerts/{id}` (ventana 30 días)

## Hyperledger Fabric

| Modo | Config | Uso |
|------|--------|-----|
| Stub | `BLOCKCHAIN_FABRIC_ENABLED=false` | Dev sin red |
| Real | `BLOCKCHAIN_FABRIC_ENABLED=true` + `fabric-up.sh` | Staging / prod |

Infra: [`infra/hyperledger-fabric/README.md`](../infra/hyperledger-fabric/README.md)

## Reintentos (RNF-16)

- `@Retryable` en `FabricGatewayLedgerAdapter` (3 intentos, backoff 2s×2)
- `BlockchainRegisterDlqConsumer` republica desde DLQ (máx. 5)

## Pendiente fase 2

- ~~Cola offline móvil para hashes ACK~~ → ver `sentinella-frontend/src/lib/mobile/blockchainAck.ts`
- ~~Hash PDFs regulatorios (reports-service)~~ → `ReportBlockchainPublisher` tras `REGULATORY_OEFA`
- Fabric CA + HSM en producción (sustituir cryptogen)
