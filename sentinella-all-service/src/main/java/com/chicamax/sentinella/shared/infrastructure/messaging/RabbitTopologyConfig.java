package com.chicamax.sentinella.shared.infrastructure.messaging;

import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.ALERT_ACKNOWLEDGED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.ALERT_CLOSED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.ALERT_CREATED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.NODE_OFFLINE_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.ROUND_SYNCED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.SENTINELLA_DLX_EXCHANGE;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.SENTINELLA_EXCHANGE;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.TELEMETRY_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.THRESHOLD_EXCEEDED_ROUTING;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class RabbitTopologyConfig {

    @Bean
    public DirectExchange sentinellaExchange() {
        return new DirectExchange(SENTINELLA_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange sentinellaDlxExchange() {
        return new DirectExchange(SENTINELLA_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue telemetryQueue() {
        return QueueBuilder.durable("telemetry.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("telemetry.dlq")
                .build();
    }

    @Bean
    public Queue thresholdExceededQueue() {
        return QueueBuilder.durable("threshold.exceeded.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("threshold.exceeded.dlq")
                .build();
    }

    @Bean
    public Queue alertCreatedQueue() {
        return QueueBuilder.durable("alert.created.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("alert.created.dlq")
                .build();
    }

    @Bean
    public Queue alertClosedQueue() {
        return QueueBuilder.durable("alert.closed.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("alert.closed.dlq")
                .build();
    }

    @Bean
    public Queue alertAcknowledgedQueue() {
        return QueueBuilder.durable("alert.acknowledged.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("alert.acknowledged.dlq")
                .build();
    }

    @Bean
    public Queue nodeOfflineQueue() {
        return QueueBuilder.durable("node.offline.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("node.offline.dlq")
                .build();
    }

    @Bean
    public Queue roundSyncedQueue() {
        return QueueBuilder.durable("round.synced.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("round.synced.dlq")
                .build();
    }

    @Bean
    public Queue telemetryDlq() {
        return QueueBuilder.durable("telemetry.dlq").build();
    }

    @Bean
    public Queue thresholdExceededDlq() {
        return QueueBuilder.durable("threshold.exceeded.dlq").build();
    }

    @Bean
    public Queue alertCreatedDlq() {
        return QueueBuilder.durable("alert.created.dlq").build();
    }

    @Bean
    public Queue alertClosedDlq() {
        return QueueBuilder.durable("alert.closed.dlq").build();
    }

    @Bean
    public Queue alertAcknowledgedDlq() {
        return QueueBuilder.durable("alert.acknowledged.dlq").build();
    }

    @Bean
    public Queue nodeOfflineDlq() {
        return QueueBuilder.durable("node.offline.dlq").build();
    }

    @Bean
    public Queue roundSyncedDlq() {
        return QueueBuilder.durable("round.synced.dlq").build();
    }

    @Bean
    public Binding telemetryBinding(
            @Qualifier("telemetryQueue") Queue telemetryQueue,
            @Qualifier("sentinellaExchange") DirectExchange sentinellaExchange
    ) {
        return BindingBuilder.bind(telemetryQueue).to(sentinellaExchange).with(TELEMETRY_ROUTING);
    }

    @Bean
    public Binding thresholdBinding(
            @Qualifier("thresholdExceededQueue") Queue thresholdExceededQueue,
            @Qualifier("sentinellaExchange") DirectExchange sentinellaExchange
    ) {
        return BindingBuilder.bind(thresholdExceededQueue).to(sentinellaExchange).with(THRESHOLD_EXCEEDED_ROUTING);
    }

    @Bean
    public Binding alertCreatedBinding(
            @Qualifier("alertCreatedQueue") Queue alertCreatedQueue,
            @Qualifier("sentinellaExchange") DirectExchange sentinellaExchange
    ) {
        return BindingBuilder.bind(alertCreatedQueue).to(sentinellaExchange).with(ALERT_CREATED_ROUTING);
    }

    @Bean
    public Binding alertClosedBinding(
            @Qualifier("alertClosedQueue") Queue alertClosedQueue,
            @Qualifier("sentinellaExchange") DirectExchange sentinellaExchange
    ) {
        return BindingBuilder.bind(alertClosedQueue).to(sentinellaExchange).with(ALERT_CLOSED_ROUTING);
    }

    @Bean
    public Binding alertAcknowledgedBinding(
            @Qualifier("alertAcknowledgedQueue") Queue alertAcknowledgedQueue,
            @Qualifier("sentinellaExchange") DirectExchange sentinellaExchange
    ) {
        return BindingBuilder.bind(alertAcknowledgedQueue).to(sentinellaExchange).with(ALERT_ACKNOWLEDGED_ROUTING);
    }

    @Bean
    public Binding nodeOfflineBinding(
            @Qualifier("nodeOfflineQueue") Queue nodeOfflineQueue,
            @Qualifier("sentinellaExchange") DirectExchange sentinellaExchange
    ) {
        return BindingBuilder.bind(nodeOfflineQueue).to(sentinellaExchange).with(NODE_OFFLINE_ROUTING);
    }

    @Bean
    public Binding roundSyncedBinding(
            @Qualifier("roundSyncedQueue") Queue roundSyncedQueue,
            @Qualifier("sentinellaExchange") DirectExchange sentinellaExchange
    ) {
        return BindingBuilder.bind(roundSyncedQueue).to(sentinellaExchange).with(ROUND_SYNCED_ROUTING);
    }

    @Bean
    public Binding telemetryDlqBinding(
            @Qualifier("telemetryDlq") Queue telemetryDlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange sentinellaDlxExchange
    ) {
        return BindingBuilder.bind(telemetryDlq).to(sentinellaDlxExchange).with("telemetry.dlq");
    }

    @Bean
    public Binding thresholdDlqBinding(
            @Qualifier("thresholdExceededDlq") Queue thresholdExceededDlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange sentinellaDlxExchange
    ) {
        return BindingBuilder.bind(thresholdExceededDlq).to(sentinellaDlxExchange).with("threshold.exceeded.dlq");
    }

    @Bean
    public Binding alertCreatedDlqBinding(
            @Qualifier("alertCreatedDlq") Queue alertCreatedDlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange sentinellaDlxExchange
    ) {
        return BindingBuilder.bind(alertCreatedDlq).to(sentinellaDlxExchange).with("alert.created.dlq");
    }

    @Bean
    public Binding alertClosedDlqBinding(
            @Qualifier("alertClosedDlq") Queue alertClosedDlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange sentinellaDlxExchange
    ) {
        return BindingBuilder.bind(alertClosedDlq).to(sentinellaDlxExchange).with("alert.closed.dlq");
    }

    @Bean
    public Binding alertAcknowledgedDlqBinding(
            @Qualifier("alertAcknowledgedDlq") Queue alertAcknowledgedDlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange sentinellaDlxExchange
    ) {
        return BindingBuilder.bind(alertAcknowledgedDlq).to(sentinellaDlxExchange).with("alert.acknowledged.dlq");
    }

    @Bean
    public Binding nodeOfflineDlqBinding(
            @Qualifier("nodeOfflineDlq") Queue nodeOfflineDlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange sentinellaDlxExchange
    ) {
        return BindingBuilder.bind(nodeOfflineDlq).to(sentinellaDlxExchange).with("node.offline.dlq");
    }

    @Bean
    public Binding roundSyncedDlqBinding(
            @Qualifier("roundSyncedDlq") Queue roundSyncedDlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange sentinellaDlxExchange
    ) {
        return BindingBuilder.bind(roundSyncedDlq).to(sentinellaDlxExchange).with("round.synced.dlq");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
