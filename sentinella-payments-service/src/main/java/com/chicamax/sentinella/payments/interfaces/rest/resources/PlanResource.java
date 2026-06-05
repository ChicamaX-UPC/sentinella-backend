package com.chicamax.sentinella.payments.interfaces.rest.resources;

import java.util.UUID;

public record PlanResource(
        UUID id,
        String code,
        String name,
        long priceCents,
        String currency,
        int sensorLimit,
        String billingPeriod
) {
}
