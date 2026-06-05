package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.util.UUID;

public record UserRegisteredMessage(UUID userId, String email, String fullName) {
}
