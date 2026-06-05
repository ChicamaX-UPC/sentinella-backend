package com.chicamax.sentinella.fieldoperations.interfaces.rest.resources;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChecklistItemResource(
        UUID id,
        String pointName,
        boolean required,
        String observations,
        String photoS3Key,
        Double latitude,
        Double longitude,
        OffsetDateTime completedAt,
        boolean anomaly
) {
}
