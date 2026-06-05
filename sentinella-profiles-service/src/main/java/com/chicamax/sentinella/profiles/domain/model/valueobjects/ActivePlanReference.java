package com.chicamax.sentinella.profiles.domain.model.valueobjects;

import java.util.UUID;

public record ActivePlanReference(String planType, int sensorLimit, UUID subscriptionId) {
}
