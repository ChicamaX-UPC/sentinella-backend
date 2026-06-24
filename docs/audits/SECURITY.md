# Auditoría de seguridad — Sentinella

Resumen de hallazgos y mitigaciones (revisión estática, jun 2026).

## Riesgos críticos (P0) — estado

| Riesgo | Estado | Mitigación aplicada |
|--------|--------|---------------------|
| Bypass de pago demo (`STRIPE_DEMO_CONFIRM`) | Mitigado | Default `false`; endpoint solo localhost si flag activo |
| API interna sin clave | Parcial | `InternalServiceKeyValidator` + flag `require-key`; activar en prod |
| Reglas de alerta sin scope | Resuelto | `GET/PUT/DELETE` filtran por nodos de `damIds` |
| Ledger blockchain global | Resuelto | `list` y `GET /{id}` validan `entityId` ∈ nodos permitidos |
| Sin enforcement de suscripción | Resuelto | `SubscriptionGuard` + `RestSubscriptionStatusClient` |
| JWT en query WebSocket | Resuelto | Ticket de un solo uso (`POST /v1/auth/ws-ticket` → `?ticket=`) |
| Logout no invalida tokens | Parcial | Denylist en IAM (`TokenRevocationFilter`); otros microservicios aceptan hasta expiración |

## Riesgos medios

- Secretos Stripe en `.env` local (gitignored pero expuesto en sync de disco).
- Registro público: flag `sentinella.auth.open-registration-enabled` (desactivar en prod).
- Swagger expuesto sin auth en algunos perfiles (condicionar a profile `dev`).

## Deuda conocida

- **Revocación global de tokens:** solo IAM valida denylist en logout. Para invalidación inmediata en todos los servicios, añadir filtro compartido en `sentinella-common` o introspección centralizada.

## Lo que ya está bien

- JWT web en cookies `httpOnly` vía BFF (`/api/backend/[...path]`).
- Mobile usa SecureStore, no `localStorage`.
- Webhooks Stripe con verificación de firma.
- SQL parametrizado (JPA); scope `damIds` / `organizationId` en nodos y alertas.
- Rate limit en login y forgot-password (`AuthRateLimiter`).

## Checklist prod

- [x] `STRIPE_DEMO_CONFIRM_ENABLED=false` (default en compose y properties)
- [ ] `SENTINELLA_SEED_ENABLED=false` (profile prod en compose)
- [ ] Clave interna obligatoria (`SENTINELLA_INTERNAL_REQUIRE_KEY=true`)
- [ ] `SENTINELLA_AUTH_OPEN_REGISTRATION=false`
- [ ] Rotar claves dev (`sentinella-internal-dev` solo local)
- [ ] Verificar que `.env` no esté en historial git
- [x] Rate limit en login / forgot-password
- [x] WebSocket sin JWT en URL (ticket de un solo uso)
