package com.chicamax.sentinella.alerts.domain.model.queries;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import java.util.Set;
import java.util.UUID;

public record GetActiveAlertsQuery(
        AlertStatus status,
        AlertSeverity severity,
        UUID nodeId,
        int page,
        int size,
        boolean scoped,
        Set<UUID> nodeIds
) {

    public int safeSize() {
        return Math.min(Math.max(size, 1), 200);
    }

    public int safePage() {
        return Math.max(page, 0);
    }
}
