package com.chicamax.sentinella.shared.infrastructure.websocket;

import com.chicamax.sentinella.alerts.domain.model.events.AlertClosedEvent;
import com.chicamax.sentinella.alerts.domain.model.events.AlertCreatedEvent;
import com.chicamax.sentinella.alerts.domain.model.events.AlertStatusUpdatedEvent;
import com.chicamax.sentinella.monitoring.domain.model.events.SensorReadingRegisteredEvent;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RealtimeEventBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeEventBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void onSensorReading(SensorReadingRegisteredEvent event) {
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

    @EventListener
    public void onAlertCreated(AlertCreatedEvent event) {
        messagingTemplate.convertAndSend("/topic/events", Map.of(
                "event", "alert.created",
                "alertId", event.alertId().toString(),
                "severity", event.severity(),
                "nodeId", event.nodeId().toString()
        ));
    }

    @EventListener
    public void onAlertClosed(AlertClosedEvent event) {
        messagingTemplate.convertAndSend("/topic/events", Map.of(
                "event", "alert.updated",
                "alertId", event.alertId().toString(),
                "status", "CLOSED",
                "nodeId", event.nodeId().toString()
        ));
    }

    @EventListener
    public void onAlertStatusUpdated(AlertStatusUpdatedEvent event) {
        messagingTemplate.convertAndSend("/topic/events", Map.of(
                "event", "alert.updated",
                "alertId", event.alertId().toString(),
                "status", event.status(),
                "nodeId", event.nodeId().toString()
        ));
    }
}
