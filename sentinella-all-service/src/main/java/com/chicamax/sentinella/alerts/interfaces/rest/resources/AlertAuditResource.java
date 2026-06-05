package com.chicamax.sentinella.alerts.interfaces.rest.resources;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertAuditResource(
        UUID id,
        UUID alertId,
        String action,
        UUID actorId,
        String actorRole,
        String notes,
        OffsetDateTime timestamp
) {
}
