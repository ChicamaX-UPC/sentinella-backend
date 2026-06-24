package com.chicamax.sentinella.shared.infrastructure.messaging.events;

public record BillingServiceSuspendedMessage(String billingEmail, String companyLabel) {
}
