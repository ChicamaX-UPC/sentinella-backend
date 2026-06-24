package com.chicamax.sentinella.monitoring.interfaces.rest.resources;

import java.util.List;
import java.util.UUID;

public record BulkNodeStatusResource(List<UUID> recentNodeIds, long totalScopedNodes) {
}
