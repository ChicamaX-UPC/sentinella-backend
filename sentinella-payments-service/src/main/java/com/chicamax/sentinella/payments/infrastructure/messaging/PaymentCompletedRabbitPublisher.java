package com.chicamax.sentinella.payments.infrastructure.messaging;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.payments.domain.model.events.PaymentCompletedEvent;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.PaymentCompletedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletedRabbitPublisher {

    private final RabbitTemplate rabbitTemplate;

    public PaymentCompletedRabbitPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(PaymentCompletedEvent event) {
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.PAYMENT_COMPLETED_ROUTING,
                new PaymentCompletedMessage(
                        event.paymentId(),
                        event.userId(),
                        event.planId(),
                        event.amount(),
                        event.currency(),
                        event.stripeSubscriptionId()
                )
        );
    }
}
