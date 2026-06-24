package com.chicamax.sentinella.plantmanagement.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateRelaveResource(
        @NotBlank String name,
        UUID tailingDamId,
        BigDecimal capacity,
        BigDecimal latitude,
        BigDecimal longitude,
        String address
) {
}
