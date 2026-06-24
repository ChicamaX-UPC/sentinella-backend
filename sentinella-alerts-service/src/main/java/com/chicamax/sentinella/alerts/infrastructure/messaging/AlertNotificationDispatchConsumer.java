package com.chicamax.sentinella.alerts.infrastructure.messaging;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.services.AlertQueryService;
import com.chicamax.sentinella.alerts.domain.services.NotificationService;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertNotificationDispatchMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AlertNotificationDispatchConsumer {

    private final AlertQueryService alertQueryService;
    private final NotificationService notificationService;

    public AlertNotificationDispatchConsumer(
            AlertQueryService alertQueryService,
            NotificationService notificationService
    ) {
        this.alertQueryService = alertQueryService;
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "alert.notification.dispatch.queue")
    public void onDispatch(AlertNotificationDispatchMessage message) {
        alertQueryService.findById(message.alertId()).ifPresent(alert -> {
            AlertChannel[] channels = parseChannels(message.channels());
            notificationService.send(alert, channels);
        });
    }

    private static AlertChannel[] parseChannels(String channelsCsv) {
        if (channelsCsv == null || channelsCsv.isBlank()) {
            return new AlertChannel[] {AlertChannel.APP};
        }
        return java.util.Arrays.stream(channelsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(AlertChannel::valueOf)
                .toArray(AlertChannel[]::new);
    }
}
