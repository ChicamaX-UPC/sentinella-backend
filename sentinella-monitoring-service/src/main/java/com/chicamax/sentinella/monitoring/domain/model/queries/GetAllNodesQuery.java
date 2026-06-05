package com.chicamax.sentinella.monitoring.domain.model.queries;

import java.util.Set;
import java.util.UUID;

public record GetAllNodesQuery(int page, int size, boolean scoped, Set<UUID> damIds) {

    public int safeSize() {
        return Math.min(Math.max(size, 1), 200);
    }

    public int safePage() {
        return Math.max(page, 0);
    }
}
