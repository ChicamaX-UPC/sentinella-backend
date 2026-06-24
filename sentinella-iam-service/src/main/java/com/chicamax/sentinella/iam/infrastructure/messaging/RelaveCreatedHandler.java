package com.chicamax.sentinella.iam.infrastructure.messaging;

import com.chicamax.sentinella.iam.domain.services.UserCommandService;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.RelaveCreatedMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RelaveCreatedHandler {

    private final UserCommandService userCommandService;

    public RelaveCreatedHandler(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @RabbitListener(queues = "relave.created.queue")
    public void onRelaveCreated(RelaveCreatedMessage message) {
        if (message.createdByUserId() == null) {
            return;
        }
        userCommandService.assignTailingDam(
                message.createdByUserId(),
                message.organizationId(),
                message.tailingDamId()
        );
    }
}
