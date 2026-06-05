package com.chicamax.sentinella.alerts.infrastructure.messaging;

import com.chicamax.sentinella.alerts.domain.model.events.AlertCreatedEvent;
import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AlertBlockchainPublisher {

    private final RabbitTemplate rabbitTemplate;

    public AlertBlockchainPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @EventListener
    public void onAlertCreated(AlertCreatedEvent event) {
        String payload = event.alertId() + ":" + event.nodeId() + ":" + event.severity();
        String hash = sha256(payload);
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.BLOCKCHAIN_REGISTER_ROUTING,
                Map.of(
                        "recordId", UUID.randomUUID(),
                        "entityType", "ALERT",
                        "entityId", event.alertId(),
                        "contentHash", hash
                )
        );
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            return value;
        }
    }
}
