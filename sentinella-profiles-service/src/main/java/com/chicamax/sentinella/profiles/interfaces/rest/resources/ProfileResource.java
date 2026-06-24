package com.chicamax.sentinella.profiles.interfaces.rest.resources;

import java.util.UUID;

public record ProfileResource(
        UUID userId,
        String email,
        String fullName,
        String companyName,
        String phone,
        String jobTitle,
        String planType,
        Integer sensorLimit,
        UUID subscriptionId,
        String preferences
) {
}
