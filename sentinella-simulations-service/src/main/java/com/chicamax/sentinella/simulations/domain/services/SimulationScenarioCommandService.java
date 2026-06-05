package com.chicamax.sentinella.simulations.domain.services;

import com.chicamax.sentinella.simulations.domain.model.aggregates.SimulationScenario;
import com.chicamax.sentinella.simulations.domain.model.commands.CreateSimulationScenarioCommand;
import com.chicamax.sentinella.simulations.domain.model.commands.UpdateSimulationScenarioCommand;
import java.util.UUID;

public interface SimulationScenarioCommandService {

    SimulationScenario create(CreateSimulationScenarioCommand command);

    SimulationScenario update(UpdateSimulationScenarioCommand command);

    void delete(UUID scenarioId, UUID actorId, String actorRole);

    SimulationScenario publish(UUID scenarioId, UUID actorId, String actorRole);

    SimulationScenario unpublish(UUID scenarioId, UUID actorId, String actorRole);
}
