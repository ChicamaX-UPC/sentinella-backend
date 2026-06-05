package com.chicamax.sentinella.plantmanagement.interfaces.rest.resources;

import com.chicamax.sentinella.plantmanagement.domain.model.valueobjects.SensorStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record NodeResource(
        UUID id,
        String externalId,
        String name,
        UUID tailingDamId,
        String sensorType,
        BigDecimal latitude,
        BigDecimal longitude,
        String position3d,
        SensorStatus status
) {
}
