package com.chicamax.sentinella.monitoring.domain.services;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.domain.model.queries.GetAllNodesQuery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorNodeQueryService {
    List<SensorNode> handle(GetAllNodesQuery query);

    Optional<SensorNode> findById(UUID nodeId);
}
