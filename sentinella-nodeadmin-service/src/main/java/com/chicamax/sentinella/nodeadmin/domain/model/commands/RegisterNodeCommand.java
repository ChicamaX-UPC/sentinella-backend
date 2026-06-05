package com.chicamax.sentinella.nodeadmin.domain.model.commands;

import java.math.BigDecimal;
import java.util.UUID;

public record RegisterNodeCommand(
        String externalId,
        String name,
        UUID tailingDamId,
        String sensorType,
        BigDecimal latitude,
        BigDecimal longitude,
        String position3d
) {
}
