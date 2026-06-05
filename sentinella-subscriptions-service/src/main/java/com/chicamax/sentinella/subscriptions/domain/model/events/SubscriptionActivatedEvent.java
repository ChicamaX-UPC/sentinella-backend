package com.chicamax.sentinella.subscriptions.domain.model.events;

import java.util.UUID;

public record SubscriptionActivatedEvent(UUID userId, UUID subscriptionId, String planType, int sensorLimit) {
}
