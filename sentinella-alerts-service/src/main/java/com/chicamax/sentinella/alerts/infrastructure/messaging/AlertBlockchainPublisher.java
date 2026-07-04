package com.chicamax.sentinella.alerts.infrastructure.messaging;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.events.AlertClosedEvent;
import com.chicamax.sentinella.alerts.domain.model.events.AlertCreatedEvent;
import com.chicamax.sentinella.alerts.domain.model.events.AlertEscalatedEvent;
import com.chicamax.sentinella.alerts.domain.model.events.AlertEvidenceUploadedEvent;
import com.chicamax.sentinella.alerts.domain.model.events.AlertStatusUpdatedEvent;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.blockchain.BlockchainHash;
import com.chicamax.sentinella.shared.infrastructure.blockchain.BlockchainRegisterMessage;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** US17 — publica hashes canonicos a la cola blockchain.register (Fabric stub o Gateway). */
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
                alert.getNodeId(),
                canonicalAlertCreated(alert)
        ));
    }

    @EventListener
    public void onAlertStatusUpdated(AlertStatusUpdatedEvent event) {
        String entityType = switch (event.status()) {
            case "ACKNOWLEDGED" -> "ALERT_ACK";
            case "COMPLETED" -> "ALERT_COMPLETED";
            default -> null;
        };
        if (entityType == null) {
            return;
        }
        alertRepository.findById(event.alertId()).ifPresent(alert -> publish(
                entityType,
                alert.getId(),
                alert.getNodeId(),
                entityType.equals("ALERT_ACK") ? canonicalAlertAck(alert) : canonicalAlertCompleted(alert)
        ));
    }

    @EventListener
    public void onAlertClosed(AlertClosedEvent event) {
        alertRepository.findById(event.alertId()).ifPresent(alert -> publish(
                "ALERT_CLOSED",
                alert.getId(),
                alert.getNodeId(),
                canonicalAlertClosed(alert)
        ));
    }

    @EventListener
    public void onAlertEscalated(AlertEscalatedEvent event) {
        alertRepository.findById(event.alertId()).ifPresent(alert -> publish(
                "ALERT_ESCALATED",
                alert.getId(),
                alert.getNodeId(),
                canonicalAlertEscalated(alert)
        ));
    }

    @EventListener
    public void onAlertEvidenceUploaded(AlertEvidenceUploadedEvent event) {
        publish(
                "ALERT_EVIDENCE",
                event.evidenceId(),
                event.nodeId(),
                event.alertId(),
                canonicalAlertEvidence(event)
        );
    }

    private void publish(String entityType, UUID entityId, UUID nodeId, String canonicalPayload) {
        publish(entityType, entityId, nodeId, entityId, canonicalPayload);
    }

    private void publish(
            String entityType,
            UUID entityId,
            UUID nodeId,
            UUID relatedEntityId,
            String canonicalPayload
    ) {
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.BLOCKCHAIN_REGISTER_ROUTING,
                BlockchainRegisterMessage.of(
                        UUID.randomUUID(),
                        entityType,
                        entityId,
                        nodeId,
                        BlockchainHash.sha256(canonicalPayload),
                        relatedEntityId
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

    private static String canonicalAlertCompleted(Alert alert) {
        return String.join("|",
                "ALERT_COMPLETED",
                alert.getId().toString(),
                alert.getNodeId().toString(),
                alert.getAssignedTo() == null ? "" : alert.getAssignedTo().toString(),
                alert.getUpdatedAt() == null ? "" : alert.getUpdatedAt().toString()
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

    private static String canonicalAlertEscalated(Alert alert) {
        return String.join("|",
                "ALERT_ESCALATED",
                alert.getId().toString(),
                alert.getNodeId().toString(),
                alert.getCreatedAt() == null ? "" : alert.getCreatedAt().toString()
        );
    }

    private static String canonicalAlertEvidence(AlertEvidenceUploadedEvent event) {
        return String.join("|",
                "ALERT_EVIDENCE",
                event.evidenceId().toString(),
                event.alertId().toString(),
                event.storageKey() == null ? "" : event.storageKey(),
                event.contentType() == null ? "" : event.contentType(),
                event.uploadedBy() == null ? "" : event.uploadedBy().toString(),
                event.uploadedAt() == null ? "" : event.uploadedAt().toString()
        );
    }
}
