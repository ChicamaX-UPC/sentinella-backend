package com.chicamax.sentinella.fieldoperations.interfaces.rest.resources;

public record CompleteChecklistItemResource(
        String observations,
        String photoS3Key,
        Double latitude,
        Double longitude,
        boolean anomaly
) {
}
