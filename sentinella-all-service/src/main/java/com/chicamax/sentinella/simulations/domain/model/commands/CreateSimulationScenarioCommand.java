package com.chicamax.sentinella.simulations.domain.model.commands;

import com.chicamax.sentinella.simulations.domain.model.valueobjects.SimulationType;
import java.util.UUID;

public record CreateSimulationScenarioCommand(
        String name,
        String description,
        SimulationType simulationType,
        String parametersJson,
        UUID tailingDamId,
        UUID createdBy
) {
}
