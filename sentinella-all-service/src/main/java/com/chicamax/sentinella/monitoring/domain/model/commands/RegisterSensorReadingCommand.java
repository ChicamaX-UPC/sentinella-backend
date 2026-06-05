package com.chicamax.sentinella.monitoring.domain.model.commands;

import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorStatus;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RegisterSensorReadingCommand(
        UUID nodeId,
        OffsetDateTime timestamp,
        SensorType sensorType,
        BigDecimal value,
        String unit,
        SensorStatus status,
        String rawPayload
) {
}
