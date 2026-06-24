package com.chicamax.sentinella.monitoring.application.internal.prediction;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.domain.model.entities.ReadingSnapshot;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.ThresholdRuleOperator;
import com.chicamax.sentinella.monitoring.domain.prediction.ChicamaBasinConstants;
import com.chicamax.sentinella.monitoring.domain.prediction.ForecastPoint;
import com.chicamax.sentinella.monitoring.domain.prediction.LinearRegression;
import com.chicamax.sentinella.monitoring.domain.prediction.NodeForecastResult;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ReadingSnapshotRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorReadingRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TrendForecastService {

    private final ReadingSnapshotRepository readingSnapshotRepository;
    private final SensorNodeRepository sensorNodeRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final RainLevelProjectionService rainLevelProjectionService;
    private final int defaultHistoryBuckets;

    public TrendForecastService(
            ReadingSnapshotRepository readingSnapshotRepository,
            SensorNodeRepository sensorNodeRepository,
            SensorReadingRepository sensorReadingRepository,
            RainLevelProjectionService rainLevelProjectionService,
            @Value("${sentinella.prediction.history-buckets:12}") int defaultHistoryBuckets
    ) {
        this.readingSnapshotRepository = readingSnapshotRepository;
        this.sensorNodeRepository = sensorNodeRepository;
        this.sensorReadingRepository = sensorReadingRepository;
        this.rainLevelProjectionService = rainLevelProjectionService;
        this.defaultHistoryBuckets = Math.max(4, defaultHistoryBuckets);
    }

    public Optional<NodeForecastResult> forecast(
            UUID nodeId,
            int horizonHours,
            BigDecimal thresholdValue,
            ThresholdRuleOperator operator
    ) {
        List<ReadingSnapshot> snapshots = readingSnapshotRepository.findTop48ByNodeIdOrderByBucketStartDesc(nodeId);
        if (snapshots.size() < 2) {
            snapshots = buildSnapshotsFromReadings(nodeId);
        }
        if (snapshots.size() < 2) {
            return Optional.empty();
        }
        snapshots = snapshots.stream()
                .sorted(Comparator.comparing(ReadingSnapshot::getBucketStart))
                .toList();
        if (snapshots.size() > defaultHistoryBuckets) {
            snapshots = snapshots.subList(snapshots.size() - defaultHistoryBuckets, snapshots.size());
        }

        List<Double> xHours = new ArrayList<>();
        List<Double> yValues = new ArrayList<>();
        OffsetDateTime t0 = snapshots.get(0).getBucketStart();
        for (ReadingSnapshot snap : snapshots) {
            double hours = Duration.between(t0, snap.getBucketStart()).toMinutes() / 60.0;
            xHours.add(hours);
            yValues.add(snap.getAvgValue().doubleValue());
        }

        LinearRegression regression = LinearRegression.fit(xHours, yValues);
        double slopePerHour = regression.slope();
        double currentValue = yValues.get(yValues.size() - 1);
        OffsetDateTime now = snapshots.get(snapshots.size() - 1).getBucketStart();

        boolean rainAdjusted = false;
        SensorNode node = sensorNodeRepository.findById(nodeId).orElse(null);
        if (node != null && node.getSensorType() == SensorType.WATER_LEVEL) {
            double rainBoost = rainLevelProjectionService.projectedRainContribution(node.getTailingDamId(), horizonHours);
            if (rainBoost > 0) {
                slopePerHour += rainBoost / Math.max(horizonHours, 1);
                rainAdjusted = true;
            }
        }

        List<ForecastPoint> points = new ArrayList<>();
        for (ReadingSnapshot snap : snapshots) {
            points.add(new ForecastPoint(snap.getBucketStart(), snap.getAvgValue(), false));
        }
        for (int h = 1; h <= horizonHours; h++) {
            OffsetDateTime ts = now.plusHours(h);
            double projected = currentValue + slopePerHour * h;
            points.add(new ForecastPoint(ts, bd(projected), true));
        }

        OffsetDateTime eta = null;
        Long leadMinutes = null;
        if (thresholdValue != null && operator != null && Math.abs(slopePerHour) > 1e-9) {
            double target = thresholdValue.doubleValue();
            if (willCross(currentValue, slopePerHour, target, operator)) {
                double hoursToBreach = (target - currentValue) / slopePerHour;
                if (hoursToBreach > 0) {
                    eta = now.plusMinutes((long) (hoursToBreach * 60));
                    leadMinutes = Duration.between(OffsetDateTime.now(), eta).toMinutes();
                }
            }
        }

        String sensorType = snapshots.get(snapshots.size() - 1).getSensorType();
        return Optional.of(new NodeForecastResult(
                nodeId,
                sensorType,
                bd(currentValue),
                bd(slopePerHour),
                thresholdValue,
                eta,
                leadMinutes,
                points,
                rainAdjusted
        ));
    }

    private List<ReadingSnapshot> buildSnapshotsFromReadings(UUID nodeId) {
        var page = sensorReadingRepository.findByNodeIdOrderByTimestampDesc(
                nodeId,
                org.springframework.data.domain.PageRequest.of(0, defaultHistoryBuckets * 2)
        );
        var readings = page.getContent();
        if (readings.size() < 2) {
            return List.of();
        }
        List<ReadingSnapshot> synthetic = new ArrayList<>();
        for (int i = readings.size() - 1; i >= 0; i--) {
            var r = readings.get(i);
            synthetic.add(new ReadingSnapshot(
                    UUID.randomUUID(),
                    nodeId,
                    r.getSensorType().name(),
                    r.getTimestamp(),
                    r.getValue(),
                    r.getValue(),
                    r.getValue(),
                    1
            ));
        }
        return synthetic;
    }

    private static boolean willCross(double current, double slope, double target, ThresholdRuleOperator operator) {
        return switch (operator) {
            case GT, GTE -> slope > 0 && current < target;
            case LT, LTE -> slope < 0 && current > target;
        };
    }

    private static BigDecimal bd(double value) {
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
    }
}
