package com.chicamax.sentinella.alerts.domain.model.commands;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertAction;
import java.util.UUID;

public record UpdateAlertCommand(
        UUID alertId,
        AlertAction action,
        UUID actorId,
        String actorRole,
        UUID assignedTo,
        String notes
) {
}
