package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.util.UUID;

public record RelaveCreatedMessage(UUID organizationId, UUID createdByUserId, UUID tailingDamId) {
}
