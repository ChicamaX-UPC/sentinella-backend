# Análisis de patrones en telemetría Sentinella

Documento de apoyo académico para el estudio de predicción con mayor anticipación (lead time).

## Fuentes de datos

| Fuente | Granularidad | Retención demo | Uso predictivo |
|--------|--------------|----------------|----------------|
| `monitoring.sensor_readings` | ~3 h (seed) / variable en prod | 90 días | Series crudas, correlaciones |
| `monitoring.reading_snapshots` | 15 min (AVG/MIN/MAX) | Sin purge | Regresión de tendencia, ETA |
| `monitoring.threshold_rules` | — | — | Umbrales objetivo para ETA |

## Patrones identificados (demo Chicama Norte)

### 1. Tendencia sinusoidal en nivel de agua
Los nodos `WATER_LEVEL` del seed usan ondas sinusoidales sobre una cota base (~782 msnm). La **pendiente instantánea** es predecible con regresión lineal sobre ventanas de 3–12 h de snapshots.

**Implicación:** un modelo de tendencia lineal estima el cruce de umbral con **6–24 h de anticipación** si la subida es sostenida.

### 2. Correlación lluvia → nivel (lag 3–9 h)
Los pluviómetros generan picos episódicos; el nivel responde con retardo según el balance hídrico del vaso (`CATCHMENT_RATIO = 2.8`, área 17.000 m²).

**Implicación:** tras intensidad pluvial > 15 mm/h sostenida, el nivel sube en las **6–12 h siguientes**. El modelo `RainLevelProjectionService` cuantifica este aporte.

### 3. Presión piezométrica — deriva lenta
`PRESSURE` varía con amplitud menor y mayor inercia. La pendiente típica es 10× menor que la del nivel.

**Implicación:** lead time de **días a semanas** para umbrales críticos; el scheduler predictivo usa ventana más larga (48 buckets = 12 h).

### 4. Estacionalidad intradiaria (operación)
Aunque el seed no modela turnos explícitos, en planta real se observan ciclos por descarga de pulpa. Con datos reales conviene descomponer serie (tendencia + estacional 24 h).

### 5. Multi-sensor: lluvia + nivel + presión
Cuando lluvia y nivel suben juntos y la presión piezométrica acelera, el riesgo hidráulico/geotécnico compuesto aumenta. El endpoint `/v1/analytics/predictive-risks` agrega nodos con ETA &lt; 12 h.

## Métricas de evaluación (reactivo vs predictivo)

| Métrica | Alerta reactiva (umbral) | Alerta predictiva (ETA) |
|---------|--------------------------|-------------------------|
| Lead time medio | ~0 min | 2–24 h (según sensor) |
| Anticipación | Ninguna | Principal ventaja |
| Falsos positivos | Baja | Mayor si la pendiente cambia |

## API de análisis

- `GET /v1/analytics/patterns` — correlación nivel↔lluvia, tasas de cambio por tipo
- `GET /v1/analytics/predictive-risks` — nodos con cruce de umbral estimado en &lt; 12 h
- `GET /v1/nodes/{id}/forecast?horizonHours=24` — proyección + ETA

## Referencias en código

- Seed sintético: `MonitoringDemoDataSeeder.java`
- Agregación: `ReadingSnapshotScheduler.java`
- Motor de tendencia: `TrendForecastService.java`
- Aporte pluvial: `RainLevelProjectionService.java`
