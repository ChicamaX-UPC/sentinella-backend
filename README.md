# Sentinella Platform (backend)

Plataforma backend de Sentinella: API orientada a dominios, mensajería entre servicios y despliegue como microservicios independientes detrás de un API Gateway.

## Requisitos

- Java 21
- Maven 3.9+
- Docker y Docker Compose (entorno local completo)

## Estructura del repositorio

Proyecto **multi-módulo Maven** (`sentinella-platform`). Servicios activos:

| Módulo | Puerto | Bounded context |
|--------|--------|-----------------|
| `sentinella-api-gateway` | 8080 | Entrada HTTP |
| `sentinella-iam-service` | 8081 | IAM |
| `sentinella-monitoring-service` | 8082 | Monitoring (umbrales, snapshots) |
| `sentinella-alerts-service` | 8083 | Alerts |
| `sentinella-plant-management-service` | 8092 | Plant Management |
| `sentinella-dashboard-service` | 8085 | Insights (read models) |
| `sentinella-reports-service` | 8086 | Reporting |
| `sentinella-simulations-service` | 8087 | Simulaciones / gemelo |
| `sentinella-profiles-service` | 8089 | Profiles |
| `sentinella-payments-service` | 8090 | Payments |
| `sentinella-subscriptions-service` | 8091 | Subscriptions |
| `sentinella-blockchain-service` | 8093 | Blockchain (stub Fabric) |

Módulos **legacy** (solo con `mvn -Plegacy`): `sentinella-fieldops-service`, `sentinella-nodeadmin-service`.

Contratos: `sentinella-contracts`, `sentinella-common`.

## Compilación

```bash
mvn -q -DskipTests package
```

Con legacy:

```bash
mvn -q -Plegacy -DskipTests package
```

## Docker Compose

```bash
docker compose up --build
```

Variables opcionales: `GOOGLE_MAPS_API_KEY`, `SENDGRID_API_KEY`, `SENTINELLA_SEED_ENABLED=true`.

Gateway: `http://localhost:8080/api/v1/...` · IAM: `http://localhost:18081`

## App móvil

Ver `../sentinella-mobile/README.md` (Flutter — pendiente de confirmación).

## Referencia

`SENTINELLA_BLUEPRINT.md` en la raíz del monorepo ChicamaX.
