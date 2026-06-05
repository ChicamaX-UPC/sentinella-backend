package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NodeOfflineRabbitMessage(UUID nodeId, OffsetDateTime since) {
}
