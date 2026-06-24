package com.chicamax.sentinella.alerts.infrastructure.messaging;

import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertCommand;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertKind;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.services.AlertCommandService;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.NodeOfflineRabbitMessage;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NodeOfflineConsumer {

    private static final UUID SYSTEM_ACTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String SYSTEM_ROLE = "SYSTEM_ADMIN";

    private final AlertCommandService alertCommandService;

    public NodeOfflineConsumer(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @RabbitListener(queues = "node.offline.queue")
    public void onNodeOffline(NodeOfflineRabbitMessage message) {
        alertCommandService.create(new CreateAlertCommand(
                null,
                message.nodeId(),
                "presence",
                BigDecimal.ZERO,
                AlertSeverity.WARNING,
                "APP",
                SYSTEM_ACTOR_ID,
                SYSTEM_ROLE,
                AlertKind.REACTIVE,
                null,
                message.since()
        ));
    }
}
