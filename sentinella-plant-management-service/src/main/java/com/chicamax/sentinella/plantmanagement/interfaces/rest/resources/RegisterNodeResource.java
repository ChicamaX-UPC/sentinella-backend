package com.chicamax.sentinella.plantmanagement.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record RegisterNodeResource(
        @NotBlank String externalId,
        @NotBlank String name,
        @NotNull UUID tailingDamId,
        @NotBlank String sensorType,
        BigDecimal latitude,
        BigDecimal longitude,
        String position3d
) {
}
