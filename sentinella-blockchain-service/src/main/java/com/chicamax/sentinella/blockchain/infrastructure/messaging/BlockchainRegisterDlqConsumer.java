package com.chicamax.sentinella.blockchain.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** RNF-16 — reintenta mensajes de la DLQ cuando Fabric estuvo temporalmente indisponible. */
@Component
public class BlockchainRegisterDlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(BlockchainRegisterDlqConsumer.class);

    private final RabbitTemplate rabbitTemplate;
    private final int maxDlqRepublish;

    public BlockchainRegisterDlqConsumer(
            RabbitTemplate rabbitTemplate,
            @Value("${sentinella.blockchain.dlq.max-republish:5}") int maxDlqRepublish
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.maxDlqRepublish = maxDlqRepublish;
    }

    @RabbitListener(queues = "blockchain.register.dlq")
    public void onDlq(Map<String, Object> message) {
        int attempt = parseAttempt(message.get("attempt"));
        if (attempt >= maxDlqRepublish) {
            log.error(
                    "Abandonando registro blockchain tras {} intentos entityType={} entityId={}",
                    attempt,
                    message.get("entityType"),
                    message.get("entityId")
            );
            return;
        }
        Map<String, Object> retry = new HashMap<>(message);
        retry.put("attempt", attempt + 1);
        log.warn(
                "Reintentando registro blockchain intento={} entityType={} entityId={}",
                attempt + 1,
                message.get("entityType"),
                message.get("entityId")
        );
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.BLOCKCHAIN_REGISTER_ROUTING,
                retry
        );
    }

    private static int parseAttempt(Object raw) {
        if (raw == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
