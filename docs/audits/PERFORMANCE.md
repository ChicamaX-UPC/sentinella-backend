# Auditoría de rendimiento — Sentinella

Resumen de cuellos de botella y acciones (jun 2026).

## Estado de mitigaciones

| Área | Estado | Notas |
|------|--------|-------|
| Dashboard N+1 | Resuelto | `GET /v1/nodes/bulk-status?since=` + cache KPI ejecutivo |
| Alertas N+1 | Resuelto | `AlertRepository.searchPaged` con `nodeIds` scoped |
| Umbrales hot path | Resuelto | `findByNodeIdAndActiveTrue` + cache Caffeine |
| Notificaciones sync | Resuelto | Cola `alert.notification.dispatch` |
| Reportes sync | Resuelto | Cola `report.generate` → HTTP 202 |
| Cola `node.offline` | Resuelto | `NodeOfflineConsumer` en alerts |
| KPI dashboard | Resuelto | Cola `dashboard.kpi.recompute` + `ExecutiveKpiCache` |
| Docker dev | Pendiente | Profile `minimal` + `mem_limit` (opcional) |

## Baseline Lighthouse (2026-06-24, dev local)

| Ruta | Perf | A11y | Best |
|------|------|------|------|
| `/` | 82 | 96 | 100 |
| `/dashboard` | 90 | 94 | 100 |
| `/monitoring` | 64 | 94 | 100 |
| `/alerts` | 64 | 94 | 100 |
| `/digital-twin` | 65 | 94 | 100 |
| `/reports` | 58 | 94 | 100 |

Detalle en [`sentinella-frontend/reports/lighthouse/SUMMARY.md`](../../sentinella-frontend/reports/lighthouse/SUMMARY.md).

## Frontend

- `dynamic()` en gemelo digital (`TwinCanvas`) y simulaciones (`SimulationsPageContent`).
- `optimizePackageImports: ['lucide-react']` en `next.config.ts`.

## Tuning Rabbit (aplicado en monitoring/alerts)

```properties
spring.rabbitmq.listener.simple.concurrency=3
spring.rabbitmq.listener.simple.max-concurrency=10
spring.rabbitmq.listener.simple.prefetch=20
```

## Medición

```bash
cd sentinella-frontend
npm run dev
npm run lighthouse:landing
npm run lighthouse:report
```
