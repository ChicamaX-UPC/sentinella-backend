package com.chicamax.sentinella.alerts.infrastructure.scheduler;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.entities.AlertAuditEntry;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import com.chicamax.sentinella.alerts.domain.services.NotificationService;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertAuditEntryRepository;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRuleRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AlertEscalationScheduler {

    private static final UUID SYSTEM_ACTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String SYSTEM_ROLE = "SYSTEM_ADMIN";

    private final AlertRepository alertRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final AlertAuditEntryRepository alertAuditEntryRepository;
    private final NotificationService notificationService;
    private final boolean enabled;

    public AlertEscalationScheduler(
            AlertRepository alertRepository,
            AlertRuleRepository alertRuleRepository,
            AlertAuditEntryRepository alertAuditEntryRepository,
            NotificationService notificationService,
            @Value("${sentinella.alerts.escalation.enabled:true}") boolean enabled
    ) {
        this.alertRepository = alertRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.alertAuditEntryRepository = alertAuditEntryRepository;
        this.notificationService = notificationService;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${sentinella.alerts.escalation.check-ms:60000}")
    @Transactional
    public void escalate() {
        if (!enabled) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        for (Alert alert : alertRepository.findByStatus(AlertStatus.ACTIVE)) {
            if (alert.getRuleId() == null) {
                continue;
            }
            if (alertAuditEntryRepository.existsByAlertIdAndAction(alert.getId(), "ESCALATED")) {
                continue;
            }
            var rule = alertRuleRepository.findById(alert.getRuleId());
            if (rule.isEmpty()) {
                continue;
            }
            var r = rule.get();
            if (r.getEscalationMinutes() == null || r.getEscalationMinutes() <= 0) {
                continue;
            }
            if (alert.getCreatedAt().plusMinutes(r.getEscalationMinutes()).isAfter(now)) {
                continue;
            }
            AlertChannel[] ch = r.decodeChannels();
            if (ch.length == 0) {
                ch = new AlertChannel[] {AlertChannel.APP};
            }
            notificationService.send(alert, ch);
            alertAuditEntryRepository.save(
                    new AlertAuditEntry(
                            UUID.randomUUID(),
                            alert.getId(),
                            "ESCALATED",
                            SYSTEM_ACTOR_ID,
                            SYSTEM_ROLE,
                            "Escalacion automatica: sin acuse en el plazo de la regla"
                    )
            );
        }
    }
}
