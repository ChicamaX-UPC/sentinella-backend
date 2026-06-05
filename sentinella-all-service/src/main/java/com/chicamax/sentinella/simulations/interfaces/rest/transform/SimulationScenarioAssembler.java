package com.chicamax.sentinella.simulations.interfaces.rest.transform;

import com.chicamax.sentinella.simulations.domain.model.aggregates.SimulationScenario;
import com.chicamax.sentinella.simulations.domain.model.commands.CreateSimulationScenarioCommand;
import com.chicamax.sentinella.simulations.domain.model.commands.UpdateSimulationScenarioCommand;
import com.chicamax.sentinella.simulations.interfaces.rest.resources.CreateSimulationScenarioResource;
import com.chicamax.sentinella.simulations.interfaces.rest.resources.SimulationScenarioResource;
import com.chicamax.sentinella.simulations.interfaces.rest.resources.UpdateSimulationScenarioResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SimulationScenarioAssembler {

    private final ObjectMapper objectMapper;

    public SimulationScenarioAssembler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SimulationScenarioResource toResource(SimulationScenario entity) {
        return new SimulationScenarioResource(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSimulationType(),
                readJson(entity.getParameters()),
                entity.getTailingDamId(),
                entity.getCreatedBy(),
                entity.isPublic(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public CreateSimulationScenarioCommand toCreateCommand(CreateSimulationScenarioResource resource, UUID createdBy) {
        return new CreateSimulationScenarioCommand(
                resource.name(),
                resource.description(),
                resource.simulationType(),
                writeJson(resource.parameters()),
                resource.tailingDamId(),
                createdBy
        );
    }

    public UpdateSimulationScenarioCommand toUpdateCommand(
            UUID scenarioId,
            UpdateSimulationScenarioResource resource,
            UUID actorId,
            String actorRole
    ) {
        return new UpdateSimulationScenarioCommand(
                scenarioId,
                resource.name(),
                resource.description(),
                resource.simulationType(),
                resource.parameters() == null ? null : writeJson(resource.parameters()),
                actorId,
                actorRole
        );
    }

    private JsonNode readJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Parametros JSON invalidos");
        }
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parametros JSON invalidos");
        }
    }
}
