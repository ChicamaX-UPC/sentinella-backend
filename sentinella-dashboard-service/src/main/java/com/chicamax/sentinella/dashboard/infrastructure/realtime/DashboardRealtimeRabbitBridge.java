package com.chicamax.sentinella.dashboard.infrastructure.realtime;

import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertAcknowledgedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertClosedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertCreatedMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorReadingReceivedMessage;
import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class DashboardRealtimeRabbitBridge {

    private final SimpMessagingTemplate messagingTemplate;

    public DashboardRealtimeRabbitBridge(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RabbitListener(queues = "realtime.sensor.reading.queue")
    public void onReading(SensorReadingReceivedMessage event) {
        messagingTemplate.convertAndSend("/topic/events", Map.of(
                "event", "sensor.reading",
                "nodeId", event.nodeId().toString(),
                "data", Map.of(
                        "timestamp", event.timestamp().toString(),
                        "type", event.sensorType(),
                        "value", event.value(),
                        "unit", event.unit(),
                        "status", event.status()
                )
        ));
    }

    @RabbitListener(queues = "realtime.alert.created.queue")
    public void onAlertCreated(AlertCreatedMessage event) {
        Map<String, Object> payload = new java.util.HashMap<>(Map.of(
                "event", "alert.created",
                "alertId", event.alertId().toString(),
                "severity", event.severity(),
                "nodeId", event.nodeId().toString()
        ));
        if (event.sensorType() != null) {
            payload.put("sensorType", event.sensorType());
        }
        if (event.triggeredValue() != null) {
            payload.put("triggeredValue", event.triggeredValue());
        }
        messagingTemplate.convertAndSend("/topic/events", payload);
    }

    @RabbitListener(queues = "realtime.alert.closed.queue")
    public void onAlertClosed(AlertClosedMessage event) {
        messagingTemplate.convertAndSend("/topic/events", Map.of(
                "event", "alert.updated",
                "alertId", event.alertId().toString(),
                "status", "CLOSED",
                "nodeId", event.nodeId().toString()
        ));
    }

    @RabbitListener(queues = "realtime.alert.acknowledged.queue")
    public void onAlertAck(AlertAcknowledgedMessage event) {
        messagingTemplate.convertAndSend("/topic/events", Map.of(
                "event", "alert.updated",
                "alertId", event.alertId().toString(),
                "status", "ACKNOWLEDGED",
                "nodeId", event.nodeId().toString()
        ));
    }
}
