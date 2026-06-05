package com.chicamax.sentinella.nodeadmin.interfaces.rest.transform;

import com.chicamax.sentinella.nodeadmin.domain.model.aggregates.IoTNode;
import com.chicamax.sentinella.nodeadmin.domain.model.commands.RegisterNodeCommand;
import com.chicamax.sentinella.nodeadmin.interfaces.rest.resources.NodeResource;
import com.chicamax.sentinella.nodeadmin.interfaces.rest.resources.RegisterNodeResource;
import org.springframework.stereotype.Component;

@Component
public class IoTNodeAssembler {

    public RegisterNodeCommand toCommand(RegisterNodeResource resource) {
        return new RegisterNodeCommand(
                resource.externalId(),
                resource.name(),
                resource.tailingDamId(),
                resource.sensorType(),
                resource.latitude(),
                resource.longitude(),
                resource.position3d()
        );
    }

    public NodeResource toResource(IoTNode node) {
        return new NodeResource(
                node.getId(),
                node.getExternalId(),
                node.getName(),
                node.getTailingDamId(),
                node.getSensorType(),
                node.getLatitude(),
                node.getLongitude(),
                node.getPosition3d(),
                node.getStatus()
        );
    }
}
