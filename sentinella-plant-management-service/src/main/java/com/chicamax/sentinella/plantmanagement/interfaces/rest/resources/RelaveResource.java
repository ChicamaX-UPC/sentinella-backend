package com.chicamax.sentinella.plantmanagement.interfaces.rest.resources;

import com.chicamax.sentinella.plantmanagement.domain.model.valueobjects.RelaveStatus;
import java.math.BigDecimal;
import java.util.UUID;

public record RelaveResource(
        UUID id,
        String name,
        UUID tailingDamId,
        BigDecimal capacity,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        RelaveStatus status
) {
}
