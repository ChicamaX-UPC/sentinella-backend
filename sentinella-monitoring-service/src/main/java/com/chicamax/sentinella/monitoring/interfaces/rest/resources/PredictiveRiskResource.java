package com.chicamax.sentinella.monitoring.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PredictiveRiskResource(
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
