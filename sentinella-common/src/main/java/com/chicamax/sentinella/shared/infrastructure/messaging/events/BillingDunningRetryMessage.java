package com.chicamax.sentinella.shared.infrastructure.messaging.events;

public record BillingDunningRetryMessage(String stripeInvoiceId) {
}
