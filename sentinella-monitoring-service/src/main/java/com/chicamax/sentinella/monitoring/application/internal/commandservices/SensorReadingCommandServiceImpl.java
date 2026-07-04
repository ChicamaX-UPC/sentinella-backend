package com.chicamax.sentinella.monitoring.application.internal.commandservices;

import com.chicamax.sentinella.monitoring.domain.model.commands.RegisterSensorReadingCommand;
import com.chicamax.sentinella.monitoring.domain.model.entities.SensorReading;
import com.chicamax.sentinella.monitoring.domain.model.events.SensorReadingRegisteredEvent;
import com.chicamax.sentinella.monitoring.domain.services.SensorReadingCommandService;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorReadingRepository;
import com.chicamax.sentinella.shared.infrastructure.observability.SentinellaMetrics;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorReadingCommandServiceImpl implements SensorReadingCommandService {

    private final SensorReadingRepository sensorReadingRepository;
    private final SensorNodeRepository sensorNodeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SentinellaMetrics sentinellaMetrics;

    public SensorReadingCommandServiceImpl(
            SensorReadingRepository sensorReadingRepository,
            SensorNodeRepository sensorNodeRepository,
            ApplicationEventPublisher eventPublisher,
            SentinellaMetrics sentinellaMetrics
    ) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.sensorNodeRepository = sensorNodeRepository;
        this.eventPublisher = eventPublisher;
        this.sentinellaMetrics = sentinellaMetrics;
    }

    @Override
    @Transactional
    public SensorReading handle(RegisterSensorReadingCommand command) {
        SensorReading reading = new SensorReading(
                UUID.randomUUID(),
                command.timestamp(),
                command.nodeId(),
                command.sensorType(),
                command.value(),
                command.unit(),
                command.status(),
                command.rawPayload()
        );
        SensorReading saved = sensorReadingRepository.save(reading);
        sensorNodeRepository.findById(saved.getNodeId()).ifPresent(node -> {
            node.recordContact(saved.getTimestamp());
            sensorNodeRepository.save(node);
        });
        sentinellaMetrics.recordIotMessageAccepted(saved.getNodeId(), saved.getSensorType().name());
        eventPublisher.publishEvent(new SensorReadingRegisteredEvent(
                saved.getId(),
                saved.getNodeId(),
                saved.getTimestamp(),
                saved.getSensorType().name(),
                saved.getValue(),
                saved.getUnit(),
                saved.getStatus().name()
        ));
        return saved;
    }
}
