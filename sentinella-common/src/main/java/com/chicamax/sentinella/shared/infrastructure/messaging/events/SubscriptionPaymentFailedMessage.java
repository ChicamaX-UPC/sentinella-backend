package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.time.OffsetDateTime;

public record SubscriptionPaymentFailedMessage(
        String stripeSubscriptionId,
        String stripeInvoiceId,
        String billingEmail,
        String failureReason,
        OffsetDateTime attemptedAt,
        OffsetDateTime nextRetryAt
) {
}
