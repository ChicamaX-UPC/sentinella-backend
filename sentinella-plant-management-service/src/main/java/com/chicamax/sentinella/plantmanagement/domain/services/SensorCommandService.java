package com.chicamax.sentinella.plantmanagement.domain.services;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Sensor;
import com.chicamax.sentinella.plantmanagement.domain.model.commands.RegisterSensorCommand;

public interface SensorCommandService {
    Sensor register(RegisterSensorCommand command);
}
