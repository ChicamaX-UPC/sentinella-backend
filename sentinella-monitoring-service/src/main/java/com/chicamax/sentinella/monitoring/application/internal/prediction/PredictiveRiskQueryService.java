package com.chicamax.sentinella.monitoring.application.internal.prediction;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.domain.model.entities.ReadingSnapshot;
import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import com.chicamax.sentinella.monitoring.domain.prediction.NodeForecastResult;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ThresholdRuleRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PredictiveRiskQueryService {

    private static final EnumSet<SensorType> FORECAST_TYPES = EnumSet.of(SensorType.WATER_LEVEL, SensorType.PRESSURE);

    private final ThresholdRuleRepository thresholdRuleRepository;
    private final SensorNodeRepository sensorNodeRepository;
    private final TrendForecastService trendForecastService;
    private final int alertWindowHours;
    private final int horizonHours;

    public PredictiveRiskQueryService(
            ThresholdRuleRepository thresholdRuleRepository,
            SensorNodeRepository sensorNodeRepository,
            TrendForecastService trendForecastService,
            @Value("${sentinella.prediction.alert-window-hours:12}") int alertWindowHours,
            @Value("${sentinella.prediction.horizon-hours:24}") int horizonHours
    ) {
        this.thresholdRuleRepository = thresholdRuleRepository;
        this.sensorNodeRepository = sensorNodeRepository;
        this.trendForecastService = trendForecastService;
        this.alertWindowHours = alertWindowHours;
        this.horizonHours = horizonHours;
    }

    public List<PredictiveRiskItem> listRisksWithinWindow() {
        List<SensorNode> forecastNodes = sensorNodeRepository.findBySensorTypeIn(FORECAST_TYPES);
        if (forecastNodes.isEmpty()) {
            return List.of();
        }
        var nodesById = forecastNodes.stream()
                .collect(java.util.stream.Collectors.toMap(SensorNode::getId, n -> n, (a, b) -> a));
        List<PredictiveRiskItem> risks = new ArrayList<>();
        for (ThresholdRule rule : thresholdRuleRepository.findByNodeIdIn(nodesById.keySet())) {
            if (!rule.isActive()) {
                continue;
            }
            SensorNode node = nodesById.get(rule.getNodeId());
            if (node == null) {
                continue;
            }
            Optional<NodeForecastResult> forecast = trendForecastService.forecast(
                    rule.getNodeId(),
                    horizonHours,
                    rule.getThresholdValue(),
                    rule.getOperator()
            );
            forecast.ifPresent(result -> {
                if (result.estimatedThresholdBreachAt() == null || result.leadTimeMinutes() == null) {
                    return;
                }
                if (result.leadTimeMinutes() > alertWindowHours * 60L) {
                    return;
                }
                risks.add(new PredictiveRiskItem(
                        rule.getId(),
                        rule.getNodeId(),
                        node.getName(),
                        node.getSensorType().name(),
                        rule.getThresholdValue(),
                        result.currentValue(),
                        result.slopePerHour(),
                        result.estimatedThresholdBreachAt(),
                        result.leadTimeMinutes(),
                        rule.getSeverity().name()
                ));
            });
        }
        risks.sort(Comparator.comparing(PredictiveRiskItem::leadTimeMinutes));
        return risks;
    }

    public record PredictiveRiskItem(
            UUID ruleId,
            UUID nodeId,
            String nodeName,
            String sensorType,
            BigDecimal thresholdValue,
            BigDecimal currentValue,
            BigDecimal slopePerHour,
            OffsetDateTime estimatedBreachAt,
            Long leadTimeMinutes,
            String severity
    ) {
    }
}
