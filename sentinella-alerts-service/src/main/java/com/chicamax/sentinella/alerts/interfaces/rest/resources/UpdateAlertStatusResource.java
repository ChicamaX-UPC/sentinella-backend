package com.chicamax.sentinella.alerts.interfaces.rest.resources;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertAction;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateAlertStatusResource(
        @NotNull AlertAction action,
        UUID assignedTo,
        String notes,
        Double latitude,
        Double longitude,
        /** Marca de tiempo del dispositivo al ACK offline; el servidor la acepta con ventana acotada. */
        OffsetDateTime clientAcknowledgedAt
) {
}
