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
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertKind;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import com.chicamax.sentinella.alerts.domain.services.AlertCommandService;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertAuditEntryRepository;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRuleRepository;
import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.AlertNotificationDispatchMessage;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.DashboardKpiRecomputeMessage;
import com.chicamax.sentinella.shared.infrastructure.observability.SentinellaMetrics;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final ApplicationEventPublisher eventPublisher;
    private final SentinellaMetrics sentinellaMetrics;
    private final RabbitTemplate rabbitTemplate;

    public AlertCommandServiceImpl(
            AlertRepository alertRepository,
            AlertAuditEntryRepository alertAuditEntryRepository,
            AlertRuleRepository alertRuleRepository,
            ApplicationEventPublisher eventPublisher,
            SentinellaMetrics sentinellaMetrics,
            RabbitTemplate rabbitTemplate
    ) {
        this.alertRepository = alertRepository;
        this.alertAuditEntryRepository = alertAuditEntryRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.eventPublisher = eventPublisher;
        this.sentinellaMetrics = sentinellaMetrics;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional
    public Alert create(CreateAlertCommand command) {
        var existing = alertRepository.findTopByRuleIdAndNodeIdAndSensorTypeAndStatusAndAlertKindOrderByCreatedAtDesc(
                command.ruleId(),
                command.nodeId(),
                command.sensorType(),
                AlertStatus.RECEIVED,
                command.alertKind() != null ? command.alertKind() : AlertKind.REACTIVE
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
                command.severity(),
                command.alertKind() != null ? command.alertKind() : AlertKind.REACTIVE,
                command.leadTimeMinutes(),
                command.estimatedBreachAt()
        );
        Alert saved = alertRepository.save(alert);
        writeAudit(saved.getId(), "CREATED", command.actorId(), command.actorRole(), null);
        sentinellaMetrics.recordAlertCreated(saved.getSeverity().name());
        dispatchNotificationAsync(saved, resolveChannels(command.notificationChannels(), command.ruleId()));
        publishKpiRecompute("alert.created", saved.getNodeId());
        eventPublisher.publishEvent(new AlertCreatedEvent(saved.getId(), saved.getNodeId(), saved.getSeverity().name()));
        return saved;
    }

    private void dispatchNotificationAsync(Alert alert, AlertChannel[] channels) {
        String channelsCsv = java.util.Arrays.stream(channels).map(Enum::name).reduce((a, b) -> a + "," + b).orElse("APP");
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.ALERT_NOTIFICATION_DISPATCH_ROUTING,
                new AlertNotificationDispatchMessage(alert.getId(), channelsCsv)
        );
    }

    @Override
    @Transactional
    public Alert update(UpdateAlertCommand command) {
        Alert alert = alertRepository.findById(command.alertId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta no encontrada"));

        if (command.action() == AlertAction.ACKNOWLEDGE) {
            alert.acknowledge(command.actorId(), resolveAcknowledgedAt(command.clientAcknowledgedAt()));
            writeAudit(alert.getId(), "ACKNOWLEDGED", command.actorId(), command.actorRole(), command.notes());
        } else if (command.action() == AlertAction.COMPLETE) {
            if (command.assignedTo() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assignedTo es requerido para COMPLETE");
            }
            alert.complete(command.assignedTo());
            writeAudit(alert.getId(), "COMPLETED", command.actorId(), command.actorRole(), command.notes());
        } else if (command.action() == AlertAction.CLOSE) {
            alert.close(command.actorId(), OffsetDateTime.now(), command.notes());
            writeAudit(alert.getId(), "CLOSED", command.actorId(), command.actorRole(), command.notes());
            eventPublisher.publishEvent(new AlertClosedEvent(alert.getId(), alert.getNodeId()));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Accion no soportada");
        }

        Alert saved = alertRepository.save(alert);
        if (command.action() == AlertAction.ACKNOWLEDGE || command.action() == AlertAction.COMPLETE) {
            eventPublisher.publishEvent(
                    new AlertStatusUpdatedEvent(saved.getId(), saved.getNodeId(), saved.getStatus().name())
            );
        }
        publishKpiRecompute("alert.updated", saved.getNodeId());
        return saved;
    }

    private void publishKpiRecompute(String reason, UUID nodeId) {
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.DASHBOARD_KPI_RECOMPUTE_ROUTING,
                new DashboardKpiRecomputeMessage(reason, nodeId)
        );
    }

    private void writeAudit(UUID alertId, String action, UUID actorId, String actorRole, String notes) {
        AlertAuditEntry entry = new AlertAuditEntry(UUID.randomUUID(), alertId, action, actorId, actorRole, notes);
        alertAuditEntryRepository.save(entry);
    }

    /** Acepta marca del dispositivo (offline) con ventana de 30 días y tolerancia de 5 min al futuro. */
    private static OffsetDateTime resolveAcknowledgedAt(OffsetDateTime clientTime) {
        if (clientTime == null) {
            return OffsetDateTime.now();
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (clientTime.isAfter(now.plusMinutes(5))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientAcknowledgedAt no puede ser futuro");
        }
        if (clientTime.isBefore(now.minusDays(30))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientAcknowledgedAt fuera de ventana offline");
        }
        return clientTime;
    }

    private AlertChannel[] resolveChannels(String channelsFromEvent, UUID ruleId) {
        if (channelsFromEvent != null && !channelsFromEvent.isBlank()) {
            return java.util.Arrays.stream(channelsFromEvent.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(AlertChannel::valueOf)
                    .toArray(AlertChannel[]::new);
        }
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
