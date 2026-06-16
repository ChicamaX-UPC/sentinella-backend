package com.chicamax.sentinella.dashboard.interfaces.rest.resources;

import java.time.OffsetDateTime;

public record FieldDashboardResource(
        long activeAlerts,
        long roundsInProgress,
        long pendingSyncRounds,
        long sensorsOutOfRange,
        OffsetDateTime lastIncidentAt
) {
}
