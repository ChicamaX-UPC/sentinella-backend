package com.chicamax.sentinella.monitoring.application.internal.prediction;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import com.chicamax.sentinella.monitoring.domain.prediction.ChicamaBasinConstants;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ReadingSnapshotRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RainLevelProjectionService {

    private final SensorNodeRepository sensorNodeRepository;
    private final ReadingSnapshotRepository readingSnapshotRepository;

    public RainLevelProjectionService(
            SensorNodeRepository sensorNodeRepository,
            ReadingSnapshotRepository readingSnapshotRepository
    ) {
        this.sensorNodeRepository = sensorNodeRepository;
        this.readingSnapshotRepository = readingSnapshotRepository;
    }

    /**
     * Estima subida adicional de cota (m) en el horizonte por lluvia reciente en el tranque.
     */
    public double projectedRainContribution(UUID tailingDamId, int horizonHours) {
        if (tailingDamId == null) {
            return 0;
        }
        List<SensorNode> pluvioNodes = sensorNodeRepository.findAll().stream()
                .filter(n -> tailingDamId.equals(n.getTailingDamId()))
                .filter(n -> n.getSensorType() == SensorType.PLUVIOMETER)
                .toList();
        if (pluvioNodes.isEmpty()) {
            return 0;
        }
        double rainMmH = 0;
        int count = 0;
        OffsetDateTime since = OffsetDateTime.now().minusHours(6);
        for (SensorNode node : pluvioNodes) {
            var snaps = readingSnapshotRepository.findByNodeIdAndBucketStartAfterOrderByBucketStartAsc(node.getId(), since);
            for (var snap : snaps) {
                rainMmH += snap.getAvgValue().doubleValue();
                count++;
            }
        }
        if (count == 0) {
            return 0;
        }
        double avgRain = rainMmH / count;
        return ChicamaBasinConstants.levelRiseFromRainMmPerHour(avgRain, horizonHours);
    }
}
