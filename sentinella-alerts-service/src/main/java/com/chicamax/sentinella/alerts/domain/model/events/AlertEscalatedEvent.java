package com.chicamax.sentinella.alerts.domain.model.events;

import java.util.UUID;

public record AlertEscalatedEvent(UUID alertId, UUID nodeId) {
}
