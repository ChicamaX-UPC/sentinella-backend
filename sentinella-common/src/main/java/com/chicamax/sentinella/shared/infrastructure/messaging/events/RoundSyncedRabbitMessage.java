package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.util.UUID;

public record RoundSyncedRabbitMessage(UUID roundId, UUID operatorId, UUID tailingDamId) {
}
