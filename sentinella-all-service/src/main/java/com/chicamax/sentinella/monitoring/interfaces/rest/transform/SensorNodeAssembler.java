package com.chicamax.sentinella.monitoring.interfaces.rest.transform;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.SensorNodeResource;
import org.springframework.stereotype.Component;

@Component
public class SensorNodeAssembler {

    public SensorNodeResource toResource(SensorNode node) {
        return new SensorNodeResource(
                node.getId(),
                node.getExternalId(),
                node.getName(),
                node.getTailingDamId(),
                node.getSensorType(),
                node.getLatitude(),
                node.getLongitude(),
                node.getPosition3d(),
                node.getStatus(),
                node.getLastSeen()
        );
    }
}
