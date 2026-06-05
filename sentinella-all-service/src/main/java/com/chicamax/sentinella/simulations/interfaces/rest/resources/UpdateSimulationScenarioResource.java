package com.chicamax.sentinella.simulations.interfaces.rest.resources;

import com.chicamax.sentinella.simulations.domain.model.valueobjects.SimulationType;
import com.fasterxml.jackson.databind.JsonNode;

public record UpdateSimulationScenarioResource(
        String name,
        String description,
        SimulationType simulationType,
        JsonNode parameters
) {
}
