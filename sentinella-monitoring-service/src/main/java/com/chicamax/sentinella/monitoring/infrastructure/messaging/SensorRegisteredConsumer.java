package com.chicamax.sentinella.monitoring.infrastructure.messaging;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorRegisteredMessage;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SensorRegisteredConsumer {

    private final SensorNodeRepository sensorNodeRepository;

    public SensorRegisteredConsumer(SensorNodeRepository sensorNodeRepository) {
        this.sensorNodeRepository = sensorNodeRepository;
    }

    @RabbitListener(queues = "sensor.registered.queue")
    @Transactional
    public void onSensorRegistered(SensorRegisteredMessage message) {
        UUID nodeId = message.nodeId() != null ? message.nodeId() : message.sensorId();
        if (sensorNodeRepository.existsById(nodeId)) {
            return;
        }
        SensorType type = parseSensorType(message.sensorType());
        SensorNode node = new SensorNode(
                nodeId,
                "NODE-" + nodeId.toString().substring(0, 8),
                "Sensor " + nodeId.toString().substring(0, 8),
                message.ownerUserId() != null ? message.ownerUserId() : UUID.fromString("00000000-0000-0000-0000-000000000010"),
                type,
                null,
                null,
                null,
                "ONLINE",
                null
        );
        sensorNodeRepository.save(node);
    }

    private static SensorType parseSensorType(String raw) {
        if (raw == null || raw.isBlank()) {
            return SensorType.WATER_LEVEL;
        }
        try {
            return SensorType.valueOf(raw.trim().toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException ex) {
            return SensorType.WATER_LEVEL;
        }
    }
}
