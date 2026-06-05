package com.chicamax.sentinella.iam.domain.model.events;

import java.util.UUID;

public record UserRegisteredEvent(UUID userId, String email) {
}
