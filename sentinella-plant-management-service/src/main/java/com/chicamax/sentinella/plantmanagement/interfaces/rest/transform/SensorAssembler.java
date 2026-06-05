package com.chicamax.sentinella.plantmanagement.interfaces.rest.transform;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Sensor;
import com.chicamax.sentinella.plantmanagement.domain.model.commands.RegisterSensorCommand;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.NodeResource;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.RegisterNodeResource;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SensorAssembler {

    public RegisterSensorCommand toCommand(RegisterNodeResource resource, UUID ownerUserId) {
        return new RegisterSensorCommand(
                resource.externalId(),
                resource.name(),
                resource.tailingDamId(),
                resource.sensorType(),
                resource.latitude(),
                resource.longitude(),
                resource.position3d(),
                ownerUserId
        );
    }

    public NodeResource toResource(Sensor sensor) {
        return new NodeResource(
                sensor.getId(),
                sensor.getExternalId(),
                sensor.getName(),
                sensor.getTailingDamId(),
                sensor.getSensorType(),
                sensor.getLatitude(),
                sensor.getLongitude(),
                sensor.getPosition3d(),
                sensor.getStatus()
        );
    }
}
