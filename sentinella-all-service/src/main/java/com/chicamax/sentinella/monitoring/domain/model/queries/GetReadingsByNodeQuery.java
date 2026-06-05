package com.chicamax.sentinella.monitoring.domain.model.queries;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GetReadingsByNodeQuery(
        UUID nodeId,
        OffsetDateTime from,
        OffsetDateTime to,
        int page,
        int size
) {
}
