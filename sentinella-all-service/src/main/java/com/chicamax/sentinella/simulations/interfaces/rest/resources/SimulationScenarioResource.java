package com.chicamax.sentinella.simulations.interfaces.rest.resources;

import com.chicamax.sentinella.simulations.domain.model.valueobjects.SimulationType;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SimulationScenarioResource(
        UUID id,
        String name,
        String description,
        SimulationType simulationType,
        JsonNode parameters,
        UUID tailingDamId,
        UUID createdBy,
        boolean isPublic,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
