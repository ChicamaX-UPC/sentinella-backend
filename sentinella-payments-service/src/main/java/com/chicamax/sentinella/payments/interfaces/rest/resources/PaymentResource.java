package com.chicamax.sentinella.payments.interfaces.rest.resources;

import java.util.UUID;

public record PaymentResource(
        UUID id,
        UUID userId,
        UUID planId,
        String status,
        long amountCents,
        String currency,
        String checkoutUrl
) {
}
