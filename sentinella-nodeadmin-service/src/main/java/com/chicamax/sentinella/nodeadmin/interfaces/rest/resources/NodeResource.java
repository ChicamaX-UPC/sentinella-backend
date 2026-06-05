package com.chicamax.sentinella.nodeadmin.interfaces.rest.resources;

import com.chicamax.sentinella.nodeadmin.domain.model.valueobjects.NodeStatus;
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
        NodeStatus status
) {
}
