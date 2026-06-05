package com.chicamax.sentinella.monitoring.domain.services;

import com.chicamax.sentinella.monitoring.domain.model.entities.SensorReading;
import com.chicamax.sentinella.monitoring.domain.model.queries.GetReadingsByNodeQuery;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface SensorReadingQueryService {
    Page<SensorReading> handle(GetReadingsByNodeQuery query);

    Optional<SensorReading> getLatestByNode(UUID nodeId);
}
