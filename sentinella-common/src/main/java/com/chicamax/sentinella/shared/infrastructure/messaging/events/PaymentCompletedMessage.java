package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCompletedMessage(
        UUID paymentId,
        UUID userId,
        UUID planId,
        BigDecimal amount,
        String currency,
        String stripeSubscriptionId
) {
    public PaymentCompletedMessage(
            UUID paymentId,
            UUID userId,
            UUID planId,
            BigDecimal amount,
            String currency
    ) {
        this(paymentId, userId, planId, amount, currency, null);
    }
}
