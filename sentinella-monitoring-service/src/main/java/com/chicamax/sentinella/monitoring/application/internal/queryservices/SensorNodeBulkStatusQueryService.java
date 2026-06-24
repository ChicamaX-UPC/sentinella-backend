package com.chicamax.sentinella.monitoring.application.internal.queryservices;

import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorReadingRepository;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.BulkNodeStatusResource;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SensorNodeBulkStatusQueryService {

    private final SensorNodeRepository sensorNodeRepository;
    private final SensorReadingRepository sensorReadingRepository;

    public SensorNodeBulkStatusQueryService(
            SensorNodeRepository sensorNodeRepository,
            SensorReadingRepository sensorReadingRepository
    ) {
        this.sensorNodeRepository = sensorNodeRepository;
        this.sensorReadingRepository = sensorReadingRepository;
    }

    public BulkNodeStatusResource getBulkStatus(boolean scoped, Collection<UUID> damIds, OffsetDateTime since) {
        long totalScopedNodes = sensorNodeRepository.countScoped(scoped, damIds);
        if (totalScopedNodes == 0) {
            return new BulkNodeStatusResource(List.of(), 0);
        }
        List<UUID> recentNodeIds = sensorReadingRepository.findRecentNodeIds(scoped, damIds, since);
        return new BulkNodeStatusResource(recentNodeIds, totalScopedNodes);
    }
}
