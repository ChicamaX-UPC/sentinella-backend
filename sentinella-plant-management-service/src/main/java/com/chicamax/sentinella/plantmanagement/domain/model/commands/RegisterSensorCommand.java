package com.chicamax.sentinella.plantmanagement.domain.model.commands;

import java.math.BigDecimal;
import java.util.UUID;

public record RegisterSensorCommand(
        String externalId,
        String name,
        UUID tailingDamId,
        String sensorType,
        BigDecimal latitude,
        BigDecimal longitude,
        String position3d,
        UUID ownerUserId
) {
}
