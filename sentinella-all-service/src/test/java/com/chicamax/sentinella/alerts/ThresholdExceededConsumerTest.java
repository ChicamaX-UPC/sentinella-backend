package com.chicamax.sentinella.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertCommand;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.services.AlertCommandService;
import com.chicamax.sentinella.alerts.infrastructure.messaging.ThresholdExceededConsumer;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.ThresholdExceededMessage;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ThresholdExceededConsumerTest {

    @Mock
    private AlertCommandService alertCommandService;

    @Test
    void shouldTranslateThresholdMessageToCreateAlertCommand() {
        ThresholdExceededConsumer consumer = new ThresholdExceededConsumer(alertCommandService);
        ThresholdExceededMessage message = new ThresholdExceededMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "water_level",
                BigDecimal.valueOf(12.7),
                "CRITICAL"
        );

        consumer.onThresholdExceeded(message);

        ArgumentCaptor<CreateAlertCommand> captor = ArgumentCaptor.forClass(CreateAlertCommand.class);
        verify(alertCommandService).create(captor.capture());
        CreateAlertCommand command = captor.getValue();

        assertEquals(message.ruleId(), command.ruleId());
        assertEquals(message.nodeId(), command.nodeId());
        assertEquals(message.sensorType(), command.sensorType());
        assertEquals(0, message.value().compareTo(command.triggeredValue()));
        assertEquals(AlertSeverity.CRITICAL, command.severity());
    }
}
