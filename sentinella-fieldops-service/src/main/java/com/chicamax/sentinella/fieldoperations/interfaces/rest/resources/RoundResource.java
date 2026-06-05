package com.chicamax.sentinella.fieldoperations.interfaces.rest.resources;

import com.chicamax.sentinella.fieldoperations.domain.model.valueobjects.RoundStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record RoundResource(
        UUID id,
        UUID operatorId,
        UUID tailingDamId,
        OffsetDateTime scheduledAt,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        RoundStatus status,
        boolean offlineCreated,
        OffsetDateTime syncedAt,
        List<ChecklistItemResource> checklistItems
) {
}
