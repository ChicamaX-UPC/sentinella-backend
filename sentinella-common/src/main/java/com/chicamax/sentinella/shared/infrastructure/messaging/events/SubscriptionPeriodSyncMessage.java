package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.time.OffsetDateTime;

public record SubscriptionPeriodSyncMessage(
        String stripeSubscriptionId,
        String billingEmail,
        OffsetDateTime currentPeriodEnd,
        boolean paymentRecovered
) {
    public SubscriptionPeriodSyncMessage(String stripeSubscriptionId, String billingEmail, OffsetDateTime currentPeriodEnd) {
        this(stripeSubscriptionId, billingEmail, currentPeriodEnd, false);
    }
}
