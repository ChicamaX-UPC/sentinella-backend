package com.chicamax.sentinella.alerts.domain.model.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertEvidenceUploadedEvent(
        UUID evidenceId,
        UUID alertId,
        UUID nodeId,
        String storageKey,
        String contentType,
        UUID uploadedBy,
        OffsetDateTime uploadedAt
) {
}
