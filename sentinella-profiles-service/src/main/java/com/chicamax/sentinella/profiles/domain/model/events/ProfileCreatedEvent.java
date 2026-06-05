package com.chicamax.sentinella.profiles.domain.model.events;

import java.util.UUID;

public record ProfileCreatedEvent(UUID userId, String email) {
}
