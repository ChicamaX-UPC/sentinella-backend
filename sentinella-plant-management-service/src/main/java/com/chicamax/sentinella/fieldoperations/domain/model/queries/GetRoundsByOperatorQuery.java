package com.chicamax.sentinella.fieldoperations.domain.model.queries;

import java.util.UUID;

public record GetRoundsByOperatorQuery(UUID operatorId, int page, int size) {

    public int safeSize() {
        return Math.min(Math.max(size, 1), 200);
    }

    public int safePage() {
        return Math.max(page, 0);
    }
}
