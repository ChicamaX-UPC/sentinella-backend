package com.chicamax.sentinella.payments.domain.model.events;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCompletedEvent(UUID paymentId, UUID userId, UUID planId, BigDecimal amount, String currency) {
}
