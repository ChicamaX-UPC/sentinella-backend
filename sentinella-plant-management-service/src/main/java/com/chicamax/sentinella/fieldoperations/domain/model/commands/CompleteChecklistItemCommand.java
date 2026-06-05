package com.chicamax.sentinella.fieldoperations.domain.model.commands;

import java.util.UUID;

public record CompleteChecklistItemCommand(
        UUID roundId,
        UUID itemId,
        String observations,
        String photoS3Key,
        Double latitude,
        Double longitude,
        boolean anomaly
) {
}
