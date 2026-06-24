package com.chicamax.sentinella.monitoring.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record NodeForecastResource(
        UUID nodeId,
        String sensorType,
        BigDecimal currentValue,
        BigDecimal slopePerHour,
        BigDecimal thresholdValue,
        OffsetDateTime estimatedThresholdBreachAt,
        Long leadTimeMinutes,
        List<ForecastPointResource> points,
        boolean rainAdjusted
) {
}
