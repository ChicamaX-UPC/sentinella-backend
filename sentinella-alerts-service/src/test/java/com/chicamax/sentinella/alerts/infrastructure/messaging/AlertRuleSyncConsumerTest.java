package com.chicamax.sentinella.alerts.infrastructure.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chicamax.sentinella.alerts.domain.model.entities.AlertRule;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertRuleOperator;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRuleRepository;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertRuleSyncMessage;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlertRuleSyncConsumerTest {

    @Mock
    private AlertRuleRepository alertRuleRepository;

    @InjectMocks
    private AlertRuleSyncConsumer consumer;

    @Test
    void shouldMapHighSeverityToCriticalOnCreate() {
        UUID ruleId = UUID.randomUUID();
        UUID nodeId = UUID.randomUUID();
        when(alertRuleRepository.findById(ruleId)).thenReturn(Optional.empty());

        consumer.onAlertRuleSync(new AlertRuleSyncMessage(
                ruleId,
                nodeId,
                "water_level",
                "GTE",
                new BigDecimal("85.0"),
                "HIGH",
                "APP",
                30,
                true
        ));

        ArgumentCaptor<AlertRule> captor = ArgumentCaptor.forClass(AlertRule.class);
        verify(alertRuleRepository).save(captor.capture());
        AlertRule saved = captor.getValue();
        assertEquals(AlertSeverity.CRITICAL, saved.getSeverity());
        assertEquals(AlertRuleOperator.GTE, saved.getOperator());
        assertEquals(nodeId, saved.getNodeId());
    }

    @Test
    void shouldDeactivateExistingRule() {
        UUID ruleId = UUID.randomUUID();
        AlertRule existing = new AlertRule(
                ruleId,
                UUID.randomUUID(),
                "pressure",
                AlertRuleOperator.GT,
                new BigDecimal("120.0"),
                AlertSeverity.CRITICAL,
                new AlertChannel[] {AlertChannel.APP},
                null,
                null
        );
        when(alertRuleRepository.findById(ruleId)).thenReturn(Optional.of(existing));

        consumer.onAlertRuleSync(new AlertRuleSyncMessage(
                ruleId,
                existing.getNodeId(),
                "pressure",
                "GT",
                new BigDecimal("120.0"),
                "CRITICAL",
                "APP",
                null,
                false
        ));

        verify(alertRuleRepository).save(existing);
        assertFalse(existing.isActive());
    }
}
