package com.chicamax.sentinella.simulations.application.internal.commandservices;

import com.chicamax.sentinella.simulations.domain.model.aggregates.SimulationScenario;
import com.chicamax.sentinella.simulations.domain.model.commands.CreateSimulationScenarioCommand;
import com.chicamax.sentinella.simulations.domain.model.commands.UpdateSimulationScenarioCommand;
import com.chicamax.sentinella.simulations.domain.services.SimulationScenarioCommandService;
import com.chicamax.sentinella.simulations.infrastructure.persistence.jpa.SimulationScenarioRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SimulationScenarioCommandServiceImpl implements SimulationScenarioCommandService {

    private final SimulationScenarioRepository repository;

    public SimulationScenarioCommandServiceImpl(SimulationScenarioRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public SimulationScenario create(CreateSimulationScenarioCommand command) {
        SimulationScenario scenario = new SimulationScenario(
                UUID.randomUUID(),
                command.name(),
                command.description(),
                command.simulationType(),
                command.parametersJson(),
                command.tailingDamId(),
                command.createdBy()
        );
        return repository.save(scenario);
    }

    @Override
    @Transactional
    public SimulationScenario update(UpdateSimulationScenarioCommand command) {
        SimulationScenario scenario = repository.findById(command.scenarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escenario no encontrado"));
        scenario.assertCreatorOrSystemAdmin(command.actorId(), command.actorRole());
        scenario.updateContent(
                command.name(),
                command.description(),
                command.simulationType(),
                command.parametersJson()
        );
        return repository.save(scenario);
    }

    @Override
    @Transactional
    public void delete(UUID scenarioId, UUID actorId, String actorRole) {
        SimulationScenario scenario = repository.findById(scenarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escenario no encontrado"));
        scenario.assertCreatorOrSystemAdmin(actorId, actorRole);
        repository.delete(scenario);
    }

    @Override
    @Transactional
    public SimulationScenario publish(UUID scenarioId, UUID actorId, String actorRole) {
        SimulationScenario scenario = repository.findById(scenarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escenario no encontrado"));
        scenario.assertCreatorOrSystemAdmin(actorId, actorRole);
        scenario.publish();
        return repository.save(scenario);
    }

    @Override
    @Transactional
    public SimulationScenario unpublish(UUID scenarioId, UUID actorId, String actorRole) {
        SimulationScenario scenario = repository.findById(scenarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Escenario no encontrado"));
        scenario.assertCreatorOrSystemAdmin(actorId, actorRole);
        scenario.unpublish();
        return repository.save(scenario);
    }
}
