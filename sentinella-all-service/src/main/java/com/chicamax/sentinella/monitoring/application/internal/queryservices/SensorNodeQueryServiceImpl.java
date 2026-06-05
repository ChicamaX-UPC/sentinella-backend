package com.chicamax.sentinella.monitoring.application.internal.queryservices;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.domain.model.queries.GetAllNodesQuery;
import com.chicamax.sentinella.monitoring.domain.services.SensorNodeQueryService;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SensorNodeQueryServiceImpl implements SensorNodeQueryService {

    private final SensorNodeRepository sensorNodeRepository;

    public SensorNodeQueryServiceImpl(SensorNodeRepository sensorNodeRepository) {
        this.sensorNodeRepository = sensorNodeRepository;
    }

    @Override
    public List<SensorNode> handle(GetAllNodesQuery query) {
        return sensorNodeRepository.findAll();
    }

    @Override
    public Optional<SensorNode> findById(UUID nodeId) {
        return sensorNodeRepository.findById(nodeId);
    }
}
