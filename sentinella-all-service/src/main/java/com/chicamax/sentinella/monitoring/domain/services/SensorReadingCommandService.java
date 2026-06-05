package com.chicamax.sentinella.monitoring.domain.services;

import com.chicamax.sentinella.monitoring.domain.model.commands.RegisterSensorReadingCommand;
import com.chicamax.sentinella.monitoring.domain.model.entities.SensorReading;

public interface SensorReadingCommandService {
    SensorReading handle(RegisterSensorReadingCommand command);
}
