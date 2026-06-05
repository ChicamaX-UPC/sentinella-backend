package com.chicamax.sentinella.monitoring.domain.services;

import com.chicamax.sentinella.monitoring.domain.model.entities.SensorReading;
import com.chicamax.sentinella.monitoring.domain.model.queries.GetReadingsByNodeQuery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorReadingQueryService {
    List<SensorReading> handle(GetReadingsByNodeQuery query);

    Optional<SensorReading> getLatestByNode(UUID nodeId);
}
