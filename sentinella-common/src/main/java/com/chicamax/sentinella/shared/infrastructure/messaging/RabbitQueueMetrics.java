package com.chicamax.sentinella.shared.infrastructure.messaging;

import com.rabbitmq.client.Channel;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnBean(RabbitTemplate.class)
public class RabbitQueueMetrics {

    private static final String[] PRIMARY_QUEUES = {
            "telemetry.queue",
            "threshold.exceeded.queue",
            "sensor.reading.persisted.queue",
            "realtime.sensor.reading.queue",
            "alert.created.queue",
            "realtime.alert.created.queue",
            "alert.closed.queue",
            "realtime.alert.closed.queue",
            "alert.acknowledged.queue",
            "realtime.alert.acknowledged.queue",
            "node.offline.queue",
            "round.synced.queue"
    };

    public RabbitQueueMetrics(MeterRegistry registry, RabbitTemplate rabbitTemplate) {
        for (String name : PRIMARY_QUEUES) {
            final String queueName = name;
            Gauge.builder("queue_messages_pending", rabbitTemplate, (RabbitTemplate rt) -> messageCountOrZero(rt, queueName))
                    .description("Mensajes pendientes de consumo por cola")
                    .tag("queue", name)
                    .register(registry);
        }
    }

    private static double messageCountOrZero(RabbitTemplate rt, String queue) {
        try {
            return rt.execute((Channel channel) -> (double) channel.queueDeclarePassive(queue).getMessageCount());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
