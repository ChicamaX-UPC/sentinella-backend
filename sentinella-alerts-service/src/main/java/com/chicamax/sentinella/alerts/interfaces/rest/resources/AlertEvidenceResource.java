package com.chicamax.sentinella.alerts.interfaces.rest.resources;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertEvidenceResource(
        UUID id,
        UUID alertId,
        String storageKey,
        String contentType,
        UUID uploadedBy,
        OffsetDateTime uploadedAt
) {
}
