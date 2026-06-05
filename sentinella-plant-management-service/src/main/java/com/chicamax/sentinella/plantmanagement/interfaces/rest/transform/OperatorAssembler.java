package com.chicamax.sentinella.plantmanagement.interfaces.rest.transform;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Operator;
import com.chicamax.sentinella.plantmanagement.domain.model.commands.RegisterOperatorCommand;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.OperatorResource;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.RegisterOperatorResource;
import org.springframework.stereotype.Component;

@Component
public class OperatorAssembler {

    public RegisterOperatorCommand toCommand(RegisterOperatorResource resource) {
        return new RegisterOperatorCommand(resource.ownerId(), resource.fullName(), resource.email());
    }

    public OperatorResource toResource(Operator operator) {
        return new OperatorResource(operator.getId(), operator.getOwnerId(), operator.getFullName(), operator.getEmail());
    }
}
