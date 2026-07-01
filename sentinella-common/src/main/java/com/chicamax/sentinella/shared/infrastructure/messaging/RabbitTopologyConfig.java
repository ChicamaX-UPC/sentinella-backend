package com.chicamax.sentinella.shared.infrastructure.messaging;

import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.ALERT_ACKNOWLEDGED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.ALERT_CLOSED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.ALERT_CREATED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.ALERT_NOTIFICATION_DISPATCH_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.ALERT_PREDICTIVE_TRIGGERED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.ALERT_TRIGGERED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.BLOCKCHAIN_REGISTER_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.DASHBOARD_KPI_RECOMPUTE_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.NODE_OFFLINE_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.REPORT_GENERATE_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.PAYMENT_COMPLETED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.ROUND_SYNCED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.SENSOR_READING_PERSISTED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.SENSOR_READING_REALTIME_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.SENSOR_REGISTERED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.SENTINELLA_DLX_EXCHANGE;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.SENTINELLA_EXCHANGE;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.SUBSCRIPTION_ACTIVATED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.SUBSCRIPTION_CANCELLED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.SUBSCRIPTION_STRIPE_CANCELLED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.SUBSCRIPTION_PERIOD_SYNC_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.SUBSCRIPTION_PAYMENT_FAILED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.BILLING_RENEWAL_REMINDER_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.BILLING_SERVICE_SUSPENDED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.BILLING_DUNNING_RETRY_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.RELAVE_CREATED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.PROFILE_PLAN_CLEARED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.TELEMETRY_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.THRESHOLD_EXCEEDED_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.ALERT_RULE_SYNC_ROUTING;
import static com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants.USER_REGISTERED_ROUTING;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public Queue alertRuleSyncQueue() {
        return QueueBuilder.durable("alert.rule.sync.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("alert.rule.sync.dlq")
                .build();
    }

    @Bean
    public Queue alertRuleSyncDlq() {
        return QueueBuilder.durable("alert.rule.sync.dlq").build();
    }

    @Bean
    public Queue thresholdExceededQueue() {
        return QueueBuilder.durable("threshold.exceeded.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("threshold.exceeded.dlq")
                .build();
    }

    @Bean
    public Queue sensorReadingPersistedQueue() {
        return QueueBuilder.durable("sensor.reading.persisted.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("sensor.reading.persisted.dlq")
                .build();
    }

    @Bean
    public Queue realtimeSensorReadingQueue() {
        return QueueBuilder.durable("realtime.sensor.reading.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("sensor.reading.realtime.dlq")
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
    public Queue realtimeAlertCreatedQueue() {
        return QueueBuilder.durable("realtime.alert.created.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("realtime.alert.created.dlq")
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
    public Queue realtimeAlertClosedQueue() {
        return QueueBuilder.durable("realtime.alert.closed.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("realtime.alert.closed.dlq")
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
    public Queue realtimeAlertAcknowledgedQueue() {
        return QueueBuilder.durable("realtime.alert.acknowledged.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("realtime.alert.acknowledged.dlq")
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
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable("user.registered.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("user.registered.dlq")
                .build();
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable("payment.completed.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("payment.completed.dlq")
                .build();
    }

    @Bean
    public Queue subscriptionActivatedQueue() {
        return QueueBuilder.durable("subscription.activated.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("subscription.activated.dlq")
                .build();
    }

    @Bean
    public Queue subscriptionCancelledQueue() {
        return QueueBuilder.durable("subscription.cancelled.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("subscription.cancelled.dlq")
                .build();
    }

    @Bean
    public Queue subscriptionStripeCancelledQueue() {
        return QueueBuilder.durable("subscription.stripe.cancelled.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("subscription.stripe.cancelled.dlq")
                .build();
    }

    @Bean
    public Queue profilePlanClearedQueue() {
        return QueueBuilder.durable("profile.plan.cleared.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("profile.plan.cleared.dlq")
                .build();
    }

    @Bean
    public Queue subscriptionPeriodSyncQueue() {
        return QueueBuilder.durable("subscription.period.sync.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("subscription.period.sync.dlq")
                .build();
    }

    @Bean
    public Queue subscriptionPaymentFailedQueue() {
        return QueueBuilder.durable("subscription.payment.failed.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("subscription.payment.failed.dlq")
                .build();
    }

    @Bean
    public Queue billingRenewalReminderQueue() {
        return QueueBuilder.durable("billing.renewal.reminder.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("billing.renewal.reminder.dlq")
                .build();
    }

    @Bean
    public Queue billingServiceSuspendedQueue() {
        return QueueBuilder.durable("billing.service.suspended.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("billing.service.suspended.dlq")
                .build();
    }

    @Bean
    public Queue billingDunningRetryQueue() {
        return QueueBuilder.durable("billing.dunning.retry.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("billing.dunning.retry.dlq")
                .build();
    }

    @Bean
    public Queue relaveCreatedQueue() {
        return QueueBuilder.durable("relave.created.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("relave.created.dlq")
                .build();
    }

    @Bean
    public Queue sensorRegisteredQueue() {
        return QueueBuilder.durable("sensor.registered.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("sensor.registered.dlq")
                .build();
    }

    @Bean
    public Queue alertTriggeredQueue() {
        return QueueBuilder.durable("alert.triggered.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("alert.triggered.dlq")
                .build();
    }

    @Bean
    public Queue alertPredictiveTriggeredQueue() {
        return QueueBuilder.durable("alert.predictive.triggered.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("alert.predictive.triggered.dlq")
                .build();
    }

    @Bean
    public Queue blockchainRegisterQueue() {
        return QueueBuilder.durable("blockchain.register.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("blockchain.register.dlq")
                .build();
    }

    @Bean
    public Queue alertNotificationDispatchQueue() {
        return QueueBuilder.durable("alert.notification.dispatch.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("alert.notification.dispatch.dlq")
                .build();
    }

    @Bean
    public Queue reportGenerateQueue() {
        return QueueBuilder.durable("report.generate.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("report.generate.dlq")
                .build();
    }

    @Bean
    public Queue dashboardKpiRecomputeQueue() {
        return QueueBuilder.durable("dashboard.kpi.recompute.queue")
                .deadLetterExchange(SENTINELLA_DLX_EXCHANGE)
                .deadLetterRoutingKey("dashboard.kpi.recompute.dlq")
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
    public Queue sensorReadingPersistedDlq() {
        return QueueBuilder.durable("sensor.reading.persisted.dlq").build();
    }

    @Bean
    public Queue sensorReadingRealtimeDlq() {
        return QueueBuilder.durable("sensor.reading.realtime.dlq").build();
    }

    @Bean
    public Queue alertCreatedDlq() {
        return QueueBuilder.durable("alert.created.dlq").build();
    }

    @Bean
    public Queue realtimeAlertCreatedDlq() {
        return QueueBuilder.durable("realtime.alert.created.dlq").build();
    }

    @Bean
    public Queue alertClosedDlq() {
        return QueueBuilder.durable("alert.closed.dlq").build();
    }

    @Bean
    public Queue realtimeAlertClosedDlq() {
        return QueueBuilder.durable("realtime.alert.closed.dlq").build();
    }

    @Bean
    public Queue alertAcknowledgedDlq() {
        return QueueBuilder.durable("alert.acknowledged.dlq").build();
    }

    @Bean
    public Queue realtimeAlertAcknowledgedDlq() {
        return QueueBuilder.durable("realtime.alert.acknowledged.dlq").build();
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
    public Queue userRegisteredDlq() {
        return QueueBuilder.durable("user.registered.dlq").build();
    }

    @Bean
    public Queue paymentCompletedDlq() {
        return QueueBuilder.durable("payment.completed.dlq").build();
    }

    @Bean
    public Queue subscriptionActivatedDlq() {
        return QueueBuilder.durable("subscription.activated.dlq").build();
    }

    @Bean
    public Queue subscriptionCancelledDlq() {
        return QueueBuilder.durable("subscription.cancelled.dlq").build();
    }

    @Bean
    public Queue sensorRegisteredDlq() {
        return QueueBuilder.durable("sensor.registered.dlq").build();
    }

    @Bean
    public Queue alertTriggeredDlq() {
        return QueueBuilder.durable("alert.triggered.dlq").build();
    }

    @Bean
    public Queue alertPredictiveTriggeredDlq() {
        return QueueBuilder.durable("alert.predictive.triggered.dlq").build();
    }

    @Bean
    public Queue blockchainRegisterDlq() {
        return QueueBuilder.durable("blockchain.register.dlq").build();
    }

    @Bean
    public Queue alertNotificationDispatchDlq() {
        return QueueBuilder.durable("alert.notification.dispatch.dlq").build();
    }

    @Bean
    public Queue reportGenerateDlq() {
        return QueueBuilder.durable("report.generate.dlq").build();
    }

    @Bean
    public Queue dashboardKpiRecomputeDlq() {
        return QueueBuilder.durable("dashboard.kpi.recompute.dlq").build();
    }

    @Bean
    public Binding telemetryBinding(
            @Qualifier("telemetryQueue") Queue telemetryQueue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(telemetryQueue).to(exchange).with(TELEMETRY_ROUTING);
    }

    @Bean
    public Binding alertRuleSyncBinding(
            @Qualifier("alertRuleSyncQueue") Queue alertRuleSyncQueue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(alertRuleSyncQueue).to(exchange).with(ALERT_RULE_SYNC_ROUTING);
    }

    @Bean
    public Binding alertRuleSyncDlqBinding(
            @Qualifier("alertRuleSyncDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("alert.rule.sync.dlq");
    }

    @Bean
    public Binding thresholdBinding(
            @Qualifier("thresholdExceededQueue") Queue thresholdExceededQueue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(thresholdExceededQueue).to(exchange).with(THRESHOLD_EXCEEDED_ROUTING);
    }

    @Bean
    public Binding sensorReadingPersistedBinding(
            @Qualifier("sensorReadingPersistedQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(SENSOR_READING_PERSISTED_ROUTING);
    }

    @Bean
    public Binding sensorReadingRealtimeBinding(
            @Qualifier("realtimeSensorReadingQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(SENSOR_READING_REALTIME_ROUTING);
    }

    @Bean
    public Binding alertCreatedBinding(
            @Qualifier("alertCreatedQueue") Queue alertCreatedQueue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(alertCreatedQueue).to(exchange).with(ALERT_CREATED_ROUTING);
    }

    @Bean
    public Binding realtimeAlertCreatedBinding(
            @Qualifier("realtimeAlertCreatedQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(ALERT_CREATED_ROUTING);
    }

    @Bean
    public Binding alertClosedBinding(
            @Qualifier("alertClosedQueue") Queue alertClosedQueue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(alertClosedQueue).to(exchange).with(ALERT_CLOSED_ROUTING);
    }

    @Bean
    public Binding realtimeAlertClosedBinding(
            @Qualifier("realtimeAlertClosedQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(ALERT_CLOSED_ROUTING);
    }

    @Bean
    public Binding alertAcknowledgedBinding(
            @Qualifier("alertAcknowledgedQueue") Queue alertAcknowledgedQueue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(alertAcknowledgedQueue).to(exchange).with(ALERT_ACKNOWLEDGED_ROUTING);
    }

    @Bean
    public Binding realtimeAlertAcknowledgedBinding(
            @Qualifier("realtimeAlertAcknowledgedQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(ALERT_ACKNOWLEDGED_ROUTING);
    }

    @Bean
    public Binding nodeOfflineBinding(
            @Qualifier("nodeOfflineQueue") Queue nodeOfflineQueue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(nodeOfflineQueue).to(exchange).with(NODE_OFFLINE_ROUTING);
    }

    @Bean
    public Binding roundSyncedBinding(
            @Qualifier("roundSyncedQueue") Queue roundSyncedQueue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(roundSyncedQueue).to(exchange).with(ROUND_SYNCED_ROUTING);
    }

    @Bean
    public Binding userRegisteredBinding(
            @Qualifier("userRegisteredQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(USER_REGISTERED_ROUTING);
    }

    @Bean
    public Binding paymentCompletedBinding(
            @Qualifier("paymentCompletedQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(PAYMENT_COMPLETED_ROUTING);
    }

    @Bean
    public Binding subscriptionActivatedBinding(
            @Qualifier("subscriptionActivatedQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(SUBSCRIPTION_ACTIVATED_ROUTING);
    }

    @Bean
    public Binding subscriptionCancelledBinding(
            @Qualifier("subscriptionCancelledQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(SUBSCRIPTION_CANCELLED_ROUTING);
    }

    @Bean
    public Binding subscriptionStripeCancelledBinding(
            @Qualifier("subscriptionStripeCancelledQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(SUBSCRIPTION_STRIPE_CANCELLED_ROUTING);
    }

    @Bean
    public Binding profilePlanClearedBinding(
            @Qualifier("profilePlanClearedQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(PROFILE_PLAN_CLEARED_ROUTING);
    }

    @Bean
    public Binding subscriptionPeriodSyncBinding(
            @Qualifier("subscriptionPeriodSyncQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(SUBSCRIPTION_PERIOD_SYNC_ROUTING);
    }

    @Bean
    public Binding subscriptionPaymentFailedBinding(
            @Qualifier("subscriptionPaymentFailedQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(SUBSCRIPTION_PAYMENT_FAILED_ROUTING);
    }

    @Bean
    public Binding billingRenewalReminderBinding(
            @Qualifier("billingRenewalReminderQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(BILLING_RENEWAL_REMINDER_ROUTING);
    }

    @Bean
    public Binding billingServiceSuspendedBinding(
            @Qualifier("billingServiceSuspendedQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(BILLING_SERVICE_SUSPENDED_ROUTING);
    }

    @Bean
    public Binding billingDunningRetryBinding(
            @Qualifier("billingDunningRetryQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(BILLING_DUNNING_RETRY_ROUTING);
    }

    @Bean
    public Binding relaveCreatedBinding(
            @Qualifier("relaveCreatedQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(RELAVE_CREATED_ROUTING);
    }

    @Bean
    public Binding sensorRegisteredBinding(
            @Qualifier("sensorRegisteredQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(SENSOR_REGISTERED_ROUTING);
    }

    @Bean
    public Binding alertTriggeredBinding(
            @Qualifier("alertTriggeredQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(ALERT_TRIGGERED_ROUTING);
    }

    @Bean
    public Binding alertPredictiveTriggeredBinding(
            @Qualifier("alertPredictiveTriggeredQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(ALERT_PREDICTIVE_TRIGGERED_ROUTING);
    }

    @Bean
    public Binding blockchainRegisterBinding(
            @Qualifier("blockchainRegisterQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(BLOCKCHAIN_REGISTER_ROUTING);
    }

    @Bean
    public Binding alertNotificationDispatchBinding(
            @Qualifier("alertNotificationDispatchQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(ALERT_NOTIFICATION_DISPATCH_ROUTING);
    }

    @Bean
    public Binding reportGenerateBinding(
            @Qualifier("reportGenerateQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(REPORT_GENERATE_ROUTING);
    }

    @Bean
    public Binding dashboardKpiRecomputeBinding(
            @Qualifier("dashboardKpiRecomputeQueue") Queue queue,
            @Qualifier("sentinellaExchange") DirectExchange exchange
    ) {
        return BindingBuilder.bind(queue).to(exchange).with(DASHBOARD_KPI_RECOMPUTE_ROUTING);
    }

    @Bean
    public Binding telemetryDlqBinding(
            @Qualifier("telemetryDlq") Queue telemetryDlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(telemetryDlq).to(dlxExchange).with("telemetry.dlq");
    }

    @Bean
    public Binding thresholdDlqBinding(
            @Qualifier("thresholdExceededDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("threshold.exceeded.dlq");
    }

    @Bean
    public Binding sensorReadingPersistedDlqBinding(
            @Qualifier("sensorReadingPersistedDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("sensor.reading.persisted.dlq");
    }

    @Bean
    public Binding sensorReadingRealtimeDlqBinding(
            @Qualifier("sensorReadingRealtimeDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("sensor.reading.realtime.dlq");
    }

    @Bean
    public Binding alertCreatedDlqBinding(
            @Qualifier("alertCreatedDlq") Queue alertCreatedDlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(alertCreatedDlq).to(dlxExchange).with("alert.created.dlq");
    }

    @Bean
    public Binding realtimeAlertCreatedDlqBinding(
            @Qualifier("realtimeAlertCreatedDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("realtime.alert.created.dlq");
    }

    @Bean
    public Binding alertClosedDlqBinding(
            @Qualifier("alertClosedDlq") Queue alertClosedDlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(alertClosedDlq).to(dlxExchange).with("alert.closed.dlq");
    }

    @Bean
    public Binding realtimeAlertClosedDlqBinding(
            @Qualifier("realtimeAlertClosedDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("realtime.alert.closed.dlq");
    }

    @Bean
    public Binding alertAcknowledgedDlqBinding(
            @Qualifier("alertAcknowledgedDlq") Queue alertAcknowledgedDlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(alertAcknowledgedDlq).to(dlxExchange).with("alert.acknowledged.dlq");
    }

    @Bean
    public Binding realtimeAlertAcknowledgedDlqBinding(
            @Qualifier("realtimeAlertAcknowledgedDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("realtime.alert.acknowledged.dlq");
    }

    @Bean
    public Binding nodeOfflineDlqBinding(
            @Qualifier("nodeOfflineDlq") Queue nodeOfflineDlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(nodeOfflineDlq).to(dlxExchange).with("node.offline.dlq");
    }

    @Bean
    public Binding roundSyncedDlqBinding(
            @Qualifier("roundSyncedDlq") Queue roundSyncedDlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(roundSyncedDlq).to(dlxExchange).with("round.synced.dlq");
    }

    @Bean
    public Binding userRegisteredDlqBinding(
            @Qualifier("userRegisteredDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("user.registered.dlq");
    }

    @Bean
    public Binding paymentCompletedDlqBinding(
            @Qualifier("paymentCompletedDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("payment.completed.dlq");
    }

    @Bean
    public Binding subscriptionActivatedDlqBinding(
            @Qualifier("subscriptionActivatedDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("subscription.activated.dlq");
    }

    @Bean
    public Binding subscriptionCancelledDlqBinding(
            @Qualifier("subscriptionCancelledDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("subscription.cancelled.dlq");
    }

    @Bean
    public Binding sensorRegisteredDlqBinding(
            @Qualifier("sensorRegisteredDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("sensor.registered.dlq");
    }

    @Bean
    public Binding alertTriggeredDlqBinding(
            @Qualifier("alertTriggeredDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("alert.triggered.dlq");
    }

    @Bean
    public Binding alertPredictiveTriggeredDlqBinding(
            @Qualifier("alertPredictiveTriggeredDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("alert.predictive.triggered.dlq");
    }

    @Bean
    public Binding blockchainRegisterDlqBinding(
            @Qualifier("blockchainRegisterDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("blockchain.register.dlq");
    }

    @Bean
    public Binding alertNotificationDispatchDlqBinding(
            @Qualifier("alertNotificationDispatchDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("alert.notification.dispatch.dlq");
    }

    @Bean
    public Binding reportGenerateDlqBinding(
            @Qualifier("reportGenerateDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("report.generate.dlq");
    }

    @Bean
    public Binding dashboardKpiRecomputeDlqBinding(
            @Qualifier("dashboardKpiRecomputeDlq") Queue dlq,
            @Qualifier("sentinellaDlxExchange") DirectExchange dlxExchange
    ) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("dashboard.kpi.recompute.dlq");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
