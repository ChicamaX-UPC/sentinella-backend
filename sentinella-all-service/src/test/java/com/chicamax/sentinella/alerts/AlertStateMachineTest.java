package com.chicamax.sentinella.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AlertStateMachineTest {

    @Test
    void mustFollowStrictStateTransitions() {
        Alert alert = new Alert(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "water_level",
                BigDecimal.valueOf(10.5),
                AlertSeverity.CRITICAL
        );

        assertThrows(ResponseStatusException.class, () ->
                alert.close(UUID.randomUUID(), OffsetDateTime.now(), "invalid"));

        alert.acknowledge(UUID.randomUUID(), OffsetDateTime.now());
        assertEquals(AlertStatus.ACKNOWLEDGED, alert.getStatus());

        alert.assign(UUID.randomUUID());
        assertEquals(AlertStatus.IN_PROGRESS, alert.getStatus());

        alert.close(UUID.randomUUID(), OffsetDateTime.now(), "resolved");
        assertEquals(AlertStatus.CLOSED, alert.getStatus());

        assertThrows(ResponseStatusException.class, () ->
                alert.acknowledge(UUID.randomUUID(), OffsetDateTime.now()));
        assertThrows(ResponseStatusException.class, () ->
                alert.assign(UUID.randomUUID()));
        assertThrows(ResponseStatusException.class, () ->
                alert.close(UUID.randomUUID(), OffsetDateTime.now(), "again"));
    }

    @Test
    void activeCannotAssignOrCloseDirectly() {
        Alert alert = new Alert(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "water_level",
                BigDecimal.ONE,
                AlertSeverity.WARNING
        );
        assertThrows(ResponseStatusException.class, () -> alert.assign(UUID.randomUUID()));
        assertThrows(ResponseStatusException.class, () ->
                alert.close(UUID.randomUUID(), OffsetDateTime.now(), "skip states"));
    }

    @Test
    void acknowledgedCannotCloseWithoutInProgress() {
        Alert alert = new Alert(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "pressure",
                BigDecimal.ONE,
                AlertSeverity.INFO
        );
        alert.acknowledge(UUID.randomUUID(), OffsetDateTime.now());
        assertThrows(ResponseStatusException.class, () ->
                alert.close(UUID.randomUUID(), OffsetDateTime.now(), "invalid"));
    }
}
