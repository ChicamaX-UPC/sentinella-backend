package com.chicamax.sentinella.monitoring.interfaces.rest.transform;

import com.chicamax.sentinella.monitoring.domain.model.entities.SensorReading;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.SensorReadingResource;
import org.springframework.stereotype.Component;

@Component
public class SensorReadingAssembler {

    public SensorReadingResource toResource(SensorReading reading) {
        return new SensorReadingResource(
                reading.getId(),
                reading.getNodeId(),
                reading.getTimestamp(),
                reading.getSensorType(),
                reading.getValue(),
                reading.getUnit(),
                reading.getStatus()
        );
    }
}
