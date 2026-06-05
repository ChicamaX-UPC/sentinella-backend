package com.chicamax.sentinella.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chicamax.sentinella.alerts.application.internal.commandservices.AlertCommandServiceImpl;
import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertCommand;
import com.chicamax.sentinella.alerts.domain.model.entities.AlertRule;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertRuleOperator;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import com.chicamax.sentinella.alerts.domain.services.NotificationService;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertAuditEntryRepository;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRuleRepository;
import com.chicamax.sentinella.shared.infrastructure.observability.SentinellaMetrics;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AlertNotificationDispatchTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertAuditEntryRepository alertAuditEntryRepository;

    @Mock
    private AlertRuleRepository alertRuleRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private SentinellaMetrics sentinellaMetrics;

    @Test
    void shouldSendNotificationsUsingRuleChannels() {
        UUID ruleId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        UUID alertId = UUID.randomUUID();
        CreateAlertCommand command = new CreateAlertCommand(
                ruleId,
                nodeId,
                "piezometer",
                BigDecimal.valueOf(17.8),
                AlertSeverity.CRITICAL,
                UUID.randomUUID(),
                "SYSTEM_ADMIN"
        );

        when(alertRepository.findTopByRuleIdAndNodeIdAndSensorTypeAndStatusOrderByCreatedAtDesc(
                ruleId, nodeId, "piezometer", AlertStatus.ACTIVE
        )).thenReturn(Optional.empty());

        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> {
            Alert incoming = invocation.getArgument(0);
            return new Alert(
                    alertId,
                    incoming.getRuleId(),
                    incoming.getNodeId(),
                    incoming.getSensorType(),
                    incoming.getTriggeredValue(),
                    incoming.getSeverity()
            );
        });

        AlertRule rule = new AlertRule(
                ruleId,
                nodeId,
                "piezometer",
                AlertRuleOperator.GT,
                BigDecimal.valueOf(15),
                AlertSeverity.CRITICAL,
                new AlertChannel[] {AlertChannel.SMS, AlertChannel.EMAIL},
                null,
                UUID.randomUUID()
        );
        when(alertRuleRepository.findById(ruleId)).thenReturn(Optional.of(rule));

        AlertCommandServiceImpl service = new AlertCommandServiceImpl(
                alertRepository,
                alertAuditEntryRepository,
                alertRuleRepository,
                notificationService,
                eventPublisher,
                sentinellaMetrics
        );

        Alert result = service.create(command);

        assertEquals(alertId, result.getId());
        verify(notificationService).send(
                any(Alert.class),
                argThat(channels -> channels.length == 2
                        && channels[0] == AlertChannel.SMS
                        && channels[1] == AlertChannel.EMAIL)
        );
    }
}
