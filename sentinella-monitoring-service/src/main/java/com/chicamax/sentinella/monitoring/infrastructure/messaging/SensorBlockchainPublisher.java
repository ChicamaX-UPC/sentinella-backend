package com.chicamax.sentinella.monitoring.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.monitoring.domain.model.events.SensorReadingRegisteredEvent;
import com.chicamax.sentinella.shared.infrastructure.blockchain.BlockchainHash;
import com.chicamax.sentinella.shared.infrastructure.blockchain.BlockchainRegisterMessage;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** US17 / RF-17 — ancla lecturas criticas de sensores (WARNING, CRITICAL). */
@Component
public class SensorBlockchainPublisher {

    private final RabbitTemplate rabbitTemplate;

    public SensorBlockchainPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @EventListener
    public void onCriticalReading(SensorReadingRegisteredEvent event) {
        if (!isCriticalStatus(event.status())) {
            return;
        }
        String canonical = String.join("|",
                "SENSOR_CRITICAL",
                event.readingId().toString(),
                event.nodeId().toString(),
                event.sensorType(),
                event.value().toPlainString(),
                event.unit(),
                event.status(),
                event.timestamp() == null ? "" : event.timestamp().toString()
        );
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.BLOCKCHAIN_REGISTER_ROUTING,
                BlockchainRegisterMessage.of(
                        UUID.randomUUID(),
                        "SENSOR_CRITICAL",
                        event.readingId(),
                        event.nodeId(),
                        BlockchainHash.sha256(canonical)
                )
        );
    }

    private static boolean isCriticalStatus(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.trim().toUpperCase();
        return "WARNING".equals(normalized) || "CRITICAL".equals(normalized);
    }
}
