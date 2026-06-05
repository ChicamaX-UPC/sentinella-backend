package com.chicamax.sentinella.monitoring.interfaces.rest.resources;

import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorStatus;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SensorReadingResource(
        UUID id,
        UUID nodeId,
        OffsetDateTime timestamp,
        SensorType sensorType,
        BigDecimal value,
        String unit,
        SensorStatus status
) {
}
