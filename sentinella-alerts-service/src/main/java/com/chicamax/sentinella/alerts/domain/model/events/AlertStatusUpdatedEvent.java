package com.chicamax.sentinella.alerts.domain.model.events;

import java.util.UUID;

public record AlertStatusUpdatedEvent(UUID alertId, UUID nodeId, String status) {
}
