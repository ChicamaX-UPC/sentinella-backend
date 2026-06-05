package com.chicamax.sentinella.nodeadmin.domain.services;

import com.chicamax.sentinella.nodeadmin.domain.model.aggregates.IoTNode;
import com.chicamax.sentinella.nodeadmin.domain.model.commands.RegisterNodeCommand;

public interface IoTNodeCommandService {
    IoTNode register(RegisterNodeCommand command);
}
