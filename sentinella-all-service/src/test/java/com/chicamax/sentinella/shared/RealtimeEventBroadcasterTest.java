package com.chicamax.sentinella.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import com.chicamax.sentinella.alerts.domain.model.events.AlertCreatedEvent;
import com.chicamax.sentinella.monitoring.domain.model.events.SensorReadingRegisteredEvent;
import com.chicamax.sentinella.shared.infrastructure.websocket.RealtimeEventBroadcaster;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class RealtimeEventBroadcasterTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void shouldPublishSensorReadingPayloadContract() {
        RealtimeEventBroadcaster broadcaster = new RealtimeEventBroadcaster(messagingTemplate);
        UUID nodeId = UUID.randomUUID();

        broadcaster.onSensorReading(new SensorReadingRegisteredEvent(
                nodeId,
                OffsetDateTime.parse("2026-04-22T10:15:30Z"),
                "water_level",
                BigDecimal.valueOf(12.34),
                "meters",
                "WARNING"
        ));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/events"), payloadCaptor.capture());
        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals("sensor.reading", payload.get("event"));
        assertEquals(nodeId.toString(), payload.get("nodeId"));
    }

    @Test
    void shouldPublishAlertCreatedPayloadContract() {
        RealtimeEventBroadcaster broadcaster = new RealtimeEventBroadcaster(messagingTemplate);
        UUID alertId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();

        broadcaster.onAlertCreated(new AlertCreatedEvent(alertId, nodeId, "CRITICAL"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/events"), payloadCaptor.capture());
        Map<String, Object> payload = payloadCaptor.getValue();
        assertEquals("alert.created", payload.get("event"));
        assertEquals(alertId.toString(), payload.get("alertId"));
    }
}
