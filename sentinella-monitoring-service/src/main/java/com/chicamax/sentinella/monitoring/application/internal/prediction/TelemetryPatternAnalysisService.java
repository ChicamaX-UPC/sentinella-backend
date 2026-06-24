package com.chicamax.sentinella.monitoring.application.internal.prediction;

import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ReadingSnapshotRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorReadingRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TelemetryPatternAnalysisService {

    private final SensorNodeRepository sensorNodeRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final ReadingSnapshotRepository readingSnapshotRepository;

    public TelemetryPatternAnalysisService(
            SensorNodeRepository sensorNodeRepository,
            SensorReadingRepository sensorReadingRepository,
            ReadingSnapshotRepository readingSnapshotRepository
    ) {
        this.sensorNodeRepository = sensorNodeRepository;
        this.sensorReadingRepository = sensorReadingRepository;
        this.readingSnapshotRepository = readingSnapshotRepository;
    }

    public TelemetryPatternsResult analyze(Set<UUID> damIds) {
        if (damIds == null || damIds.isEmpty()) {
            return emptyResult();
        }
        List<com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode> scopedNodes =
                sensorNodeRepository.findByTailingDamIdIn(damIds);
        if (scopedNodes.isEmpty()) {
            return emptyResult();
        }

        OffsetDateTime since = OffsetDateTime.now().minusDays(7);
        var waterNodes = scopedNodes.stream()
                .filter(n -> n.getSensorType() == SensorType.WATER_LEVEL)
                .limit(3)
                .toList();
        var rainNodes = scopedNodes.stream()
                .filter(n -> n.getSensorType() == SensorType.PLUVIOMETER)
                .limit(3)
                .toList();

        double rainLevelCorrelation = correlateWaterRain(waterNodes, rainNodes, since);
        List<RateOfChangePattern> rates = computeRatePatterns(scopedNodes, since);
        Double avgRain = readingSnapshotRepository.averageBySensorTypeSince(SensorType.PLUVIOMETER.name(), since);

        return new TelemetryPatternsResult(
                rainLevelCorrelation,
                avgRain != null ? BigDecimal.valueOf(avgRain).setScale(2, RoundingMode.HALF_UP) : null,
                rates,
                List.of(
                        "Tendencia sinusoidal en nivel: pendiente estimable con regresión lineal (6–24 h lead time).",
                        "Picos pluviométricos preceden subida de cota con lag 3–9 h en datos demo.",
                        "Presión piezométrica: deriva más lenta; ETA en días/semanas.",
                        "Multi-sensor: lluvia+nivel+presión simultáneos elevan riesgo compuesto.",
                        "Alerta predictiva anticipa cruce de umbral vs alerta reactiva (0 min)."
                )
        );
    }

    private TelemetryPatternsResult emptyResult() {
        return new TelemetryPatternsResult(0, null, List.of(), List.of());
    }

    private double correlateWaterRain(
            List<com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode> waterNodes,
            List<com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode> rainNodes,
            OffsetDateTime since
    ) {
        if (waterNodes.isEmpty() || rainNodes.isEmpty()) {
            return 0;
        }
        List<Double> water = new ArrayList<>();
        List<Double> rain = new ArrayList<>();
        for (var w : waterNodes) {
            sensorReadingRepository
                    .findByNodeIdAndTimestampGreaterThanEqualOrderByTimestampDesc(w.getId(), since, org.springframework.data.domain.PageRequest.of(0, 48))
                    .forEach(r -> water.add(r.getValue().doubleValue()));
        }
        for (var r : rainNodes) {
            sensorReadingRepository
                    .findByNodeIdAndTimestampGreaterThanEqualOrderByTimestampDesc(r.getId(), since, org.springframework.data.domain.PageRequest.of(0, 48))
                    .forEach(reading -> rain.add(reading.getValue().doubleValue()));
        }
        int n = Math.min(water.size(), rain.size());
        if (n < 3) {
            return 0;
        }
        return pearson(water.subList(0, n), rain.subList(0, n));
    }

    private List<RateOfChangePattern> computeRatePatterns(
            List<com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode> scopedNodes,
            OffsetDateTime since
    ) {
        Map<String, List<com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode>> byType =
                scopedNodes.stream()
                        .collect(Collectors.groupingBy(n -> n.getSensorType().name()));

        List<RateOfChangePattern> patterns = new ArrayList<>();
        for (var entry : byType.entrySet()) {
            DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
            for (var node : entry.getValue().stream().limit(5).toList()) {
                var page = sensorReadingRepository.findByNodeIdAndTimestampGreaterThanEqualOrderByTimestampDesc(
                        node.getId(),
                        since,
                        org.springframework.data.domain.PageRequest.of(0, 20)
                );
                var readings = page.getContent();
                for (int i = 1; i < readings.size(); i++) {
                    double delta = readings.get(i - 1).getValue().subtract(readings.get(i).getValue()).doubleValue();
                    stats.accept(Math.abs(delta));
                }
            }
            if (stats.getCount() > 0) {
                patterns.add(new RateOfChangePattern(
                        entry.getKey(),
                        BigDecimal.valueOf(stats.getAverage()).setScale(4, RoundingMode.HALF_UP),
                        BigDecimal.valueOf(stats.getMax()).setScale(4, RoundingMode.HALF_UP)
                ));
            }
        }
        return patterns;
    }

    private static double pearson(List<Double> x, List<Double> y) {
        int n = x.size();
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;
        double sumY2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += x.get(i);
            sumY += y.get(i);
            sumXY += x.get(i) * y.get(i);
            sumX2 += x.get(i) * x.get(i);
            sumY2 += y.get(i) * y.get(i);
        }
        double denom = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));
        if (denom < 1e-12) {
            return 0;
        }
        return (n * sumXY - sumX * sumY) / denom;
    }

    public record RateOfChangePattern(String sensorType, BigDecimal avgAbsDelta, BigDecimal maxAbsDelta) {
    }

    public record TelemetryPatternsResult(
            double waterRainCorrelation,
            BigDecimal avgRainMmPerHour,
            List<RateOfChangePattern> rateOfChangeByType,
            List<String> documentedPatterns
    ) {
    }
}
