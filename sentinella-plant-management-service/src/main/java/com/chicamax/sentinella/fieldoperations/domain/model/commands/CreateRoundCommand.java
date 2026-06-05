package com.chicamax.sentinella.fieldoperations.domain.model.commands;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateRoundCommand(
        UUID operatorId,
        UUID tailingDamId,
        OffsetDateTime scheduledAt,
        boolean offlineCreated
) {
}
