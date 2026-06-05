package com.chicamax.sentinella.simulations.interfaces.rest.resources;

import com.chicamax.sentinella.simulations.domain.model.valueobjects.SimulationType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateSimulationScenarioResource(
        @NotBlank String name,
        String description,
        @NotNull SimulationType simulationType,
        @NotNull JsonNode parameters,
        @NotNull UUID tailingDamId
) {
}
