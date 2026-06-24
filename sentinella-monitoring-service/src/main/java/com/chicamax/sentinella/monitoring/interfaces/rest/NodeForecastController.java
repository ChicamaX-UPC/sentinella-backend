package com.chicamax.sentinella.monitoring.interfaces.rest;

import com.chicamax.sentinella.monitoring.application.internal.prediction.TrendForecastService;
import com.chicamax.sentinella.monitoring.domain.model.entities.ReadingSnapshot;
import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import com.chicamax.sentinella.monitoring.domain.prediction.ForecastPoint;
import com.chicamax.sentinella.monitoring.domain.prediction.NodeForecastResult;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ReadingSnapshotRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ThresholdRuleRepository;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.ForecastPointResource;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.NodeForecastResource;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.ReadingSnapshotResource;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/nodes")
public class NodeForecastController {

    private final TrendForecastService trendForecastService;
    private final ReadingSnapshotRepository readingSnapshotRepository;
    private final ThresholdRuleRepository thresholdRuleRepository;
    private final MonitoringNodeAccessGuard monitoringNodeAccessGuard;
    private final int defaultHorizonHours;

    public NodeForecastController(
            TrendForecastService trendForecastService,
            ReadingSnapshotRepository readingSnapshotRepository,
            ThresholdRuleRepository thresholdRuleRepository,
            MonitoringNodeAccessGuard monitoringNodeAccessGuard,
            @Value("${sentinella.prediction.horizon-hours:24}") int defaultHorizonHours
    ) {
        this.trendForecastService = trendForecastService;
        this.readingSnapshotRepository = readingSnapshotRepository;
        this.thresholdRuleRepository = thresholdRuleRepository;
        this.monitoringNodeAccessGuard = monitoringNodeAccessGuard;
        this.defaultHorizonHours = defaultHorizonHours;
    }

    @GetMapping("/{nodeId}/snapshots")
    public ResponseEntity<List<ReadingSnapshotResource>> getSnapshots(
            @PathVariable UUID nodeId,
            @RequestParam(defaultValue = "48") int limit,
            @AuthenticationPrincipal Jwt jwt
    ) {
        monitoringNodeAccessGuard.ensureCanAccess(jwt, nodeId);
        List<ReadingSnapshot> snapshots = readingSnapshotRepository.findTop48ByNodeIdOrderByBucketStartDesc(nodeId);
        if (limit > 0 && snapshots.size() > limit) {
            snapshots = snapshots.subList(0, limit);
        }
        List<ReadingSnapshotResource> resources = snapshots.stream()
                .sorted(Comparator.comparing(ReadingSnapshot::getBucketStart))
                .map(s -> new ReadingSnapshotResource(
                        s.getId(),
                        s.getNodeId(),
                        s.getSensorType(),
                        s.getBucketStart(),
                        s.getAvgValue(),
                        s.getMinValue(),
                        s.getMaxValue(),
                        s.getSampleCount()
                ))
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{nodeId}/forecast")
    public ResponseEntity<NodeForecastResource> getForecast(
            @PathVariable UUID nodeId,
            @RequestParam(required = false) Integer horizonHours,
            @AuthenticationPrincipal Jwt jwt
    ) {
        monitoringNodeAccessGuard.ensureCanAccess(jwt, nodeId);
        int horizon = horizonHours != null ? horizonHours : defaultHorizonHours;
        ThresholdRule rule = thresholdRuleRepository.findByNodeIdAndActiveTrue(nodeId).stream()
                .findFirst()
                .orElse(null);

        NodeForecastResult result = trendForecastService.forecast(
                nodeId,
                horizon,
                rule != null ? rule.getThresholdValue() : null,
                rule != null ? rule.getOperator() : null
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Datos insuficientes para pronóstico (se requieren snapshots o lecturas recientes)"
        ));

        return ResponseEntity.ok(toResource(result));
    }

    private static NodeForecastResource toResource(NodeForecastResult result) {
        List<ForecastPointResource> points = result.points().stream()
                .map(NodeForecastController::toPoint)
                .toList();
        return new NodeForecastResource(
                result.nodeId(),
                result.sensorType(),
                result.currentValue(),
                result.slopePerHour(),
                result.thresholdValue(),
                result.estimatedThresholdBreachAt(),
                result.leadTimeMinutes(),
                points,
                result.rainAdjusted()
        );
    }

    private static ForecastPointResource toPoint(ForecastPoint point) {
        return new ForecastPointResource(point.timestamp(), point.value(), point.projected());
    }
}
