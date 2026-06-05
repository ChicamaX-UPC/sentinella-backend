package com.chicamax.sentinella.dashboard.domain.model.queries;

import java.util.UUID;
import java.util.Set;

public record GetFieldDashboardQuery(UUID userId, Set<UUID> damIds) {
}
