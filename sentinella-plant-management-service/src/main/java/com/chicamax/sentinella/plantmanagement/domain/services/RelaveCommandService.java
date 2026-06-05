package com.chicamax.sentinella.plantmanagement.domain.services;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Relave;
import com.chicamax.sentinella.plantmanagement.domain.model.commands.CreateRelaveCommand;

public interface RelaveCommandService {
    Relave create(CreateRelaveCommand command);
}
