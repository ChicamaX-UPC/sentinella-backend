package com.chicamax.sentinella.plantmanagement.domain.model.commands;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateRelaveCommand(
        String name,
        UUID tailingDamId,
        BigDecimal capacity,
        BigDecimal latitude,
        BigDecimal longitude,
        String address
) {
}
