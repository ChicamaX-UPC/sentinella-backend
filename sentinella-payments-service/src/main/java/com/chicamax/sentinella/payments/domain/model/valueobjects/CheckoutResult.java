package com.chicamax.sentinella.payments.domain.model.valueobjects;

import com.chicamax.sentinella.payments.domain.model.aggregates.Payment;
import java.util.UUID;

public record CheckoutResult(Payment payment, String checkoutUrl) {
    public UUID paymentId() {
        return payment.getId();
    }
}
