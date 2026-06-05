package com.chicamax.sentinella.subscriptions.interfaces.rest.resources;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SubscriptionResource(
        UUID id,
        UUID userId,
        UUID planId,
        String planType,
        int sensorLimit,
        String status,
        OffsetDateTime startedAt,
        OffsetDateTime expiresAt
) {
}
