package com.chicamax.sentinella.dashboard.interfaces.rest.resources;

public record ExecutiveDashboardResource(
        long totalNodes,
        long activeAlerts,
        long criticalAlerts,
        long nodesWithRecentData
) {
}
