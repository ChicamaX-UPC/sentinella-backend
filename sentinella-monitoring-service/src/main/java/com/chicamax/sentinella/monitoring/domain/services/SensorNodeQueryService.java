package com.chicamax.sentinella.monitoring.domain.services;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.domain.model.queries.GetAllNodesQuery;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface SensorNodeQueryService {
    Page<SensorNode> handle(GetAllNodesQuery query);

    Optional<SensorNode> findById(UUID nodeId);
}
