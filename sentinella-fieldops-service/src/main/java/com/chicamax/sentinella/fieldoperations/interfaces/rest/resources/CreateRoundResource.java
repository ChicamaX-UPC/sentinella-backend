package com.chicamax.sentinella.fieldoperations.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateRoundResource(
        @NotNull UUID tailingDamId,
        @NotNull OffsetDateTime scheduledAt,
        boolean offlineCreated
) {
}
