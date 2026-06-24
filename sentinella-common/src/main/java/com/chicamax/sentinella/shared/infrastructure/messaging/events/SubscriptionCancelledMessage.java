package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.util.UUID;

public record SubscriptionCancelledMessage(UUID userId, UUID subscriptionId, String stripeSubscriptionId) {
}
