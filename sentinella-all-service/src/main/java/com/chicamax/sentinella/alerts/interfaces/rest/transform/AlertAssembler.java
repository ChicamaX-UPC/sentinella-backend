package com.chicamax.sentinella.alerts.interfaces.rest.transform;

import com.chicamax.sentinella.alerts.domain.model.aggregates.Alert;
import com.chicamax.sentinella.alerts.domain.model.entities.AlertAuditEntry;
import com.chicamax.sentinella.alerts.interfaces.rest.resources.AlertAuditResource;
import com.chicamax.sentinella.alerts.interfaces.rest.resources.AlertResource;
import org.springframework.stereotype.Component;

@Component
public class AlertAssembler {

    public AlertResource toResource(Alert alert) {
        return new AlertResource(
                alert.getId(),
                alert.getRuleId(),
                alert.getNodeId(),
                alert.getSensorType(),
                alert.getTriggeredValue(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getAcknowledgedBy(),
                alert.getAcknowledgedAt(),
                alert.getAssignedTo(),
                alert.getClosedBy(),
                alert.getClosedAt(),
                alert.getResolutionNotes()
        );
    }

    public AlertAuditResource toAuditResource(AlertAuditEntry entry) {
        return new AlertAuditResource(
                entry.getId(),
                entry.getAlertId(),
                entry.getAction(),
                entry.getActorId(),
                entry.getActorRole(),
                entry.getNotes(),
                entry.getTimestamp()
        );
    }
}
