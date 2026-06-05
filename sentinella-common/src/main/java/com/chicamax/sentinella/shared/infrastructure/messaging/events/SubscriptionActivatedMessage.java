package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.util.UUID;

public record SubscriptionActivatedMessage(
        UUID userId,
        UUID subscriptionId,
        String planType,
        int sensorLimit
) {
}
