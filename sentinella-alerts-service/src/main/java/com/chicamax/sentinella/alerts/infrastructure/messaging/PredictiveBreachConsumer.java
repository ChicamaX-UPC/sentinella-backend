package com.chicamax.sentinella.alerts.infrastructure.messaging;

import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertCommand;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertKind;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.services.AlertCommandService;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.PredictiveBreachMessage;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PredictiveBreachConsumer {

    private static final UUID SYSTEM_ACTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String SYSTEM_ROLE = "SYSTEM_ADMIN";

    private final AlertCommandService alertCommandService;

    public PredictiveBreachConsumer(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @RabbitListener(queues = "alert.predictive.triggered.queue")
    public void onPredictiveBreach(PredictiveBreachMessage message) {
        alertCommandService.create(new CreateAlertCommand(
                message.ruleId(),
                message.nodeId(),
                message.sensorType(),
                message.currentValue(),
                AlertSeverity.valueOf(message.severity()),
                message.channels(),
                SYSTEM_ACTOR_ID,
                SYSTEM_ROLE,
                AlertKind.PREDICTIVE,
                message.leadTimeMinutes(),
                message.estimatedBreachAt()
        ));
    }
}
