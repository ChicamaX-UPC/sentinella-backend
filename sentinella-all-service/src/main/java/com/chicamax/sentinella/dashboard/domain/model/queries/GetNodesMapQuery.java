package com.chicamax.sentinella.dashboard.domain.model.queries;

import java.util.Set;
import java.util.UUID;

public record GetNodesMapQuery(Set<UUID> damIds) {
}
