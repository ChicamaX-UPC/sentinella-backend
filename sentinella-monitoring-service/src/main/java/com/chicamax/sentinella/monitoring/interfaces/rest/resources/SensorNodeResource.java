package com.chicamax.sentinella.monitoring.interfaces.rest.resources;

import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SensorNodeResource(
        UUID id,
        String externalId,
        String name,
        UUID tailingDamId,
        SensorType sensorType,
        BigDecimal latitude,
        BigDecimal longitude,
        String position3d,
        String status,
        OffsetDateTime lastSeen
) {
}
