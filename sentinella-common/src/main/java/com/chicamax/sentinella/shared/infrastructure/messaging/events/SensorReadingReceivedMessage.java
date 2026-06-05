package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SensorReadingReceivedMessage(
        UUID nodeId,
        OffsetDateTime timestamp,
        String sensorType,
        BigDecimal value,
        String unit,
        String status,
        String rawPayload
) {
}
