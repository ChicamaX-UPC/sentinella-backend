package com.chicamax.sentinella.alerts.infrastructure.messaging;

import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertCommand;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.services.AlertCommandService;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertTriggeredMessage;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AlertTriggeredConsumer {

    private static final UUID SYSTEM_ACTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String SYSTEM_ROLE = "SYSTEM_ADMIN";

    private final AlertCommandService alertCommandService;

    public AlertTriggeredConsumer(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @RabbitListener(queues = "alert.triggered.queue")
    public void onAlertTriggered(AlertTriggeredMessage message) {
        alertCommandService.create(new CreateAlertCommand(
                message.ruleId(),
                message.nodeId(),
                message.sensorType(),
                message.value(),
                AlertSeverity.valueOf(message.severity()),
                message.channels(),
                SYSTEM_ACTOR_ID,
                SYSTEM_ROLE
        ));
    }
}
