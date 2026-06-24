package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.time.OffsetDateTime;

public record BillingRenewalReminderMessage(
        String billingEmail,
        String companyLabel,
        OffsetDateTime chargeDate
) {
}
