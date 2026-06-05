package com.chicamax.sentinella.monitoring.infrastructure.presence;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.NodeOfflineRabbitMessage;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NodePresenceNotifier {

    private final SensorNodeRepository sensorNodeRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final int offlineAfterSeconds;
    private final Set<java.util.UUID> currentlyOffline = ConcurrentHashMap.newKeySet();

    public NodePresenceNotifier(
            SensorNodeRepository sensorNodeRepository,
            SimpMessagingTemplate messagingTemplate,
            RabbitTemplate rabbitTemplate,
            @Value("${sentinella.presence.offline-after-seconds:120}") int offlineAfterSeconds
    ) {
        this.sensorNodeRepository = sensorNodeRepository;
        this.messagingTemplate = messagingTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.offlineAfterSeconds = offlineAfterSeconds;
    }

    @Scheduled(fixedDelayString = "${sentinella.presence.check-ms:30000}")
    public void tick() {
        OffsetDateTime now = OffsetDateTime.now();
        for (SensorNode n : sensorNodeRepository.findAll()) {
            OffsetDateTime ref = n.getLastSeen() != null ? n.getLastSeen() : n.getCreatedAt();
            if (ref == null) {
                continue;
            }
            long ageSec = Math.max(0, Duration.between(ref, now).getSeconds());
            if (ageSec >= offlineAfterSeconds) {
                if (currentlyOffline.add(n.getId())) {
                    messagingTemplate.convertAndSend("/topic/events", Map.of(
                            "event", "node.offline",
                            "nodeId", n.getId().toString(),
                            "since", ref.toString()
                    ));
                    rabbitTemplate.convertAndSend(
                            SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                            SentinellaMessagingConstants.NODE_OFFLINE_ROUTING,
                            new NodeOfflineRabbitMessage(n.getId(), ref)
                    );
                }
            } else if (currentlyOffline.remove(n.getId())) {
                messagingTemplate.convertAndSend("/topic/events", Map.of(
                        "event", "node.online",
                        "nodeId", n.getId().toString()
                ));
            }
        }
    }
}
