package com.chicamax.sentinella.subscriptions.interfaces.rest.resources;

public record SubscriptionStatusResource(
        boolean active,
        SubscriptionResource subscription
) {
}
