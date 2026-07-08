package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.math.BigDecimal;
import java.util.UUID;

public record SensorRegisteredMessage(
        UUID sensorId,
        UUID nodeId,
        UUID ownerUserId,
        String sensorType,
        UUID tailingDamId,
        String externalId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String position3d
) {
}
