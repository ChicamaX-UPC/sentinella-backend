package com.chicamax.sentinella.plantmanagement.domain.services;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Operator;
import com.chicamax.sentinella.plantmanagement.domain.model.commands.RegisterOperatorCommand;

public interface OperatorCommandService {
    Operator register(RegisterOperatorCommand command);
}
