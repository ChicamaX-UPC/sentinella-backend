package com.chicamax.sentinella.monitoring.domain.prediction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record NodeForecastResult(
        UUID nodeId,
        String sensorType,
        BigDecimal currentValue,
        BigDecimal slopePerHour,
        BigDecimal thresholdValue,
        OffsetDateTime estimatedThresholdBreachAt,
        Long leadTimeMinutes,
        List<ForecastPoint> points,
        boolean rainAdjusted
) {
}
