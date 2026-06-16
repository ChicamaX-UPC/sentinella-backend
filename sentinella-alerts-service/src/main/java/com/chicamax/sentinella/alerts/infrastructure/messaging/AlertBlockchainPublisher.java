package com.chicamax.sentinella.alerts.infrastructure.messaging;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.events.AlertClosedEvent;
import com.chicamax.sentinella.alerts.domain.model.events.AlertCreatedEvent;
import com.chicamax.sentinella.alerts.domain.model.events.AlertStatusUpdatedEvent;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.blockchain.BlockchainHash;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** US17 — publica hashes canonicos a la cola blockchain.register (Fabric stub). */
@Component
public class AlertBlockchainPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AlertRepository alertRepository;

    public AlertBlockchainPublisher(RabbitTemplate rabbitTemplate, AlertRepository alertRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.alertRepository = alertRepository;
    }

    @EventListener
    public void onAlertCreated(AlertCreatedEvent event) {
        alertRepository.findById(event.alertId()).ifPresent(alert -> publish(
                "ALERT",
                alert.getId(),
                canonicalAlertCreated(alert)
        ));
    }

    @EventListener
    public void onAlertStatusUpdated(AlertStatusUpdatedEvent event) {
        if (!AlertStatus.ACKNOWLEDGED.name().equals(event.status())) {
            return;
        }
        alertRepository.findById(event.alertId()).ifPresent(alert -> publish(
                "ALERT_ACK",
                alert.getId(),
                canonicalAlertAck(alert)
        ));
    }

    @EventListener
    public void onAlertClosed(AlertClosedEvent event) {
        alertRepository.findById(event.alertId()).ifPresent(alert -> publish(
                "ALERT_CLOSED",
                alert.getId(),
                canonicalAlertClosed(alert)
        ));
    }

    private void publish(String entityType, UUID entityId, String canonicalPayload) {
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.BLOCKCHAIN_REGISTER_ROUTING,
                Map.of(
                        "recordId", UUID.randomUUID(),
                        "entityType", entityType,
                        "entityId", entityId,
                        "contentHash", BlockchainHash.sha256(canonicalPayload)
                )
        );
    }

    private static String canonicalAlertCreated(Alert alert) {
        return String.join("|",
                "ALERT",
                alert.getId().toString(),
                alert.getRuleId() == null ? "" : alert.getRuleId().toString(),
                alert.getNodeId().toString(),
                alert.getSensorType(),
                alert.getTriggeredValue().toPlainString(),
                alert.getSeverity().name(),
                alert.getCreatedAt() == null ? "" : alert.getCreatedAt().toString()
        );
    }

    private static String canonicalAlertAck(Alert alert) {
        return String.join("|",
                "ALERT_ACK",
                alert.getId().toString(),
                alert.getNodeId().toString(),
                alert.getAcknowledgedBy() == null ? "" : alert.getAcknowledgedBy().toString(),
                alert.getAcknowledgedAt() == null ? "" : alert.getAcknowledgedAt().toString()
        );
    }

    private static String canonicalAlertClosed(Alert alert) {
        return String.join("|",
                "ALERT_CLOSED",
                alert.getId().toString(),
                alert.getNodeId().toString(),
                alert.getClosedBy() == null ? "" : alert.getClosedBy().toString(),
                alert.getClosedAt() == null ? "" : alert.getClosedAt().toString(),
                alert.getResolutionNotes() == null ? "" : alert.getResolutionNotes()
        );
    }
}
