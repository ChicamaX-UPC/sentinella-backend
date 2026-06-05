package com.chicamax.sentinella.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chicamax.sentinella.alerts.application.internal.commandservices.AlertCommandServiceImpl;
import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertCommand;
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
class AlertDeduplicationTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertAuditEntryRepository alertAuditEntryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AlertRuleRepository alertRuleRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SentinellaMetrics sentinellaMetrics;

    @Test
    void shouldReturnExistingActiveAlertWhenDuplicated() {
        UUID ruleId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        Alert existing = new Alert(
                UUID.randomUUID(),
                ruleId,
                nodeId,
                "water_level",
                BigDecimal.valueOf(12.2),
                AlertSeverity.CRITICAL
        );

        when(alertRepository.findTopByRuleIdAndNodeIdAndSensorTypeAndStatusOrderByCreatedAtDesc(
                ruleId, nodeId, "water_level", AlertStatus.ACTIVE
        )).thenReturn(Optional.of(existing));

        AlertCommandServiceImpl service = new AlertCommandServiceImpl(
                alertRepository,
                alertAuditEntryRepository,
                alertRuleRepository,
                notificationService,
                eventPublisher,
                sentinellaMetrics
        );

        Alert result = service.create(new CreateAlertCommand(
                ruleId,
                nodeId,
                "water_level",
                BigDecimal.valueOf(15),
                AlertSeverity.CRITICAL,
                UUID.randomUUID(),
                "SYSTEM_ADMIN"
        ));

        assertEquals(existing.getId(), result.getId());
        verify(alertRepository, never()).save(org.mockito.ArgumentMatchers.any(Alert.class));
        verify(notificationService, never()).send(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
