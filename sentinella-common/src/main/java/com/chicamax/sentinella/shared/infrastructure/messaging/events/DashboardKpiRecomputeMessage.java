package com.chicamax.sentinella.shared.infrastructure.messaging.events;

import java.util.UUID;

public record DashboardKpiRecomputeMessage(String reason, UUID nodeId) {
}
