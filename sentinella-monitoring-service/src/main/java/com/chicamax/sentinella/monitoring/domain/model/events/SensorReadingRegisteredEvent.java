package com.chicamax.sentinella.monitoring.domain.model.events;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SensorReadingRegisteredEvent(
        UUID readingId,
        UUID nodeId,
        OffsetDateTime timestamp,
        String sensorType,
        BigDecimal value,
        String unit,
        String status
) {
}
