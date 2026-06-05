package com.chicamax.sentinella.monitoring.application.internal.queryservices;

import com.chicamax.sentinella.monitoring.domain.model.entities.SensorReading;
import com.chicamax.sentinella.monitoring.domain.model.queries.GetReadingsByNodeQuery;
import com.chicamax.sentinella.monitoring.domain.services.SensorReadingQueryService;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorReadingRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SensorReadingQueryServiceImpl implements SensorReadingQueryService {

    private final SensorReadingRepository sensorReadingRepository;

    public SensorReadingQueryServiceImpl(SensorReadingRepository sensorReadingRepository) {
        this.sensorReadingRepository = sensorReadingRepository;
    }

    @Override
    public Page<SensorReading> handle(GetReadingsByNodeQuery query) {
        int size = Math.min(Math.max(query.size(), 1), 200);
        int page = Math.max(query.page(), 0);
        var pageable = PageRequest.of(page, size);
        if (query.from() != null && query.to() != null) {
            return sensorReadingRepository.findByNodeIdAndTimestampBetweenOrderByTimestampDesc(
                    query.nodeId(),
                    query.from(),
                    query.to(),
                    pageable
            );
        }
        if (query.from() != null) {
            return sensorReadingRepository.findByNodeIdAndTimestampGreaterThanEqualOrderByTimestampDesc(
                    query.nodeId(),
                    query.from(),
                    pageable
            );
        }
        if (query.to() != null) {
            return sensorReadingRepository.findByNodeIdAndTimestampLessThanEqualOrderByTimestampDesc(
                    query.nodeId(),
                    query.to(),
                    pageable
            );
        }
        return sensorReadingRepository.findByNodeIdOrderByTimestampDesc(query.nodeId(), pageable);
    }

    @Override
    public Optional<SensorReading> getLatestByNode(UUID nodeId) {
        return sensorReadingRepository.findTopByNodeIdOrderByTimestampDesc(nodeId);
    }
}
