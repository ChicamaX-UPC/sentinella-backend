package com.chicamax.sentinella.alerts.application.internal.commandservices;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertCommand;
import com.chicamax.sentinella.alerts.domain.model.commands.UpdateAlertCommand;
import com.chicamax.sentinella.alerts.domain.model.entities.AlertAuditEntry;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.model.events.AlertClosedEvent;
import com.chicamax.sentinella.alerts.domain.model.events.AlertCreatedEvent;
import com.chicamax.sentinella.alerts.domain.model.events.AlertStatusUpdatedEvent;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertAction;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import com.chicamax.sentinella.alerts.domain.services.AlertCommandService;
import com.chicamax.sentinella.alerts.domain.services.NotificationService;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertAuditEntryRepository;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRuleRepository;
import com.chicamax.sentinella.shared.infrastructure.observability.SentinellaMetrics;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AlertCommandServiceImpl implements AlertCommandService {

    private final AlertRepository alertRepository;
    private final AlertAuditEntryRepository alertAuditEntryRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final SentinellaMetrics sentinellaMetrics;

    public AlertCommandServiceImpl(
            AlertRepository alertRepository,
            AlertAuditEntryRepository alertAuditEntryRepository,
            AlertRuleRepository alertRuleRepository,
            NotificationService notificationService,
            ApplicationEventPublisher eventPublisher,
            SentinellaMetrics sentinellaMetrics
    ) {
        this.alertRepository = alertRepository;
        this.alertAuditEntryRepository = alertAuditEntryRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.notificationService = notificationService;
        this.eventPublisher = eventPublisher;
        this.sentinellaMetrics = sentinellaMetrics;
    }

    @Override
    @Transactional
    public Alert create(CreateAlertCommand command) {
        var existing = alertRepository.findTopByRuleIdAndNodeIdAndSensorTypeAndStatusOrderByCreatedAtDesc(
                command.ruleId(),
                command.nodeId(),
                command.sensorType(),
                AlertStatus.ACTIVE
        );
        if (existing.isPresent()) {
            return existing.get();
        }

        Alert alert = new Alert(
                UUID.randomUUID(),
                command.ruleId(),
                command.nodeId(),
                command.sensorType(),
                command.triggeredValue(),
                command.severity()
        );
        Alert saved = alertRepository.save(alert);
        writeAudit(saved.getId(), "CREATED", command.actorId(), command.actorRole(), null);
        sentinellaMetrics.recordAlertCreated(saved.getSeverity().name());
        notificationService.send(saved, resolveChannels(command.ruleId()));
        eventPublisher.publishEvent(new AlertCreatedEvent(saved.getId(), saved.getNodeId(), saved.getSeverity().name()));
        return saved;
    }

    @Override
    @Transactional
    public Alert update(UpdateAlertCommand command) {
        Alert alert = alertRepository.findById(command.alertId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta no encontrada"));

        if (command.action() == AlertAction.ACKNOWLEDGE) {
            alert.acknowledge(command.actorId(), OffsetDateTime.now());
            writeAudit(alert.getId(), "ACKNOWLEDGED", command.actorId(), command.actorRole(), command.notes());
        } else if (command.action() == AlertAction.ASSIGN) {
            if (command.assignedTo() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assignedTo es requerido para ASSIGN");
            }
            alert.assign(command.assignedTo());
            writeAudit(alert.getId(), "ASSIGNED", command.actorId(), command.actorRole(), command.notes());
        } else if (command.action() == AlertAction.CLOSE) {
            alert.close(command.actorId(), OffsetDateTime.now(), command.notes());
            writeAudit(alert.getId(), "CLOSED", command.actorId(), command.actorRole(), command.notes());
            eventPublisher.publishEvent(new AlertClosedEvent(alert.getId(), alert.getNodeId()));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Accion no soportada");
        }

        Alert saved = alertRepository.save(alert);
        if (command.action() == AlertAction.ACKNOWLEDGE || command.action() == AlertAction.ASSIGN) {
            eventPublisher.publishEvent(
                    new AlertStatusUpdatedEvent(saved.getId(), saved.getNodeId(), saved.getStatus().name())
            );
        }
        return saved;
    }

    private void writeAudit(UUID alertId, String action, UUID actorId, String actorRole, String notes) {
        AlertAuditEntry entry = new AlertAuditEntry(UUID.randomUUID(), alertId, action, actorId, actorRole, notes);
        alertAuditEntryRepository.save(entry);
    }

    private AlertChannel[] resolveChannels(UUID ruleId) {
        if (ruleId == null) {
            return new AlertChannel[] {AlertChannel.APP};
        }
        return alertRuleRepository.findById(ruleId)
                .map(rule -> {
                    AlertChannel[] channels = rule.decodeChannels();
                    if (channels.length == 0) {
                        return new AlertChannel[] {AlertChannel.APP};
                    }
                    return channels;
                })
                .orElse(new AlertChannel[] {AlertChannel.APP});
    }
}
