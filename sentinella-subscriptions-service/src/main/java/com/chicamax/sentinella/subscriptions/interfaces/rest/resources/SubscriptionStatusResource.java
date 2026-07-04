package com.chicamax.sentinella.subscriptions.interfaces.rest.resources;

public record SubscriptionStatusResource(
        boolean active,
        SubscriptionResource subscription
) {
    public static SubscriptionStatusResource inactive() {
        return new SubscriptionStatusResource(false, null);
    }

    public static SubscriptionStatusResource of(SubscriptionResource subscription) {
        return new SubscriptionStatusResource(true, subscription);
    }
}
