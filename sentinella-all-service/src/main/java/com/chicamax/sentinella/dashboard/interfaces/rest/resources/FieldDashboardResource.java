package com.chicamax.sentinella.dashboard.interfaces.rest.resources;

public record FieldDashboardResource(
        long activeAlerts,
        long roundsInProgress,
        long pendingSyncRounds
) {
}
