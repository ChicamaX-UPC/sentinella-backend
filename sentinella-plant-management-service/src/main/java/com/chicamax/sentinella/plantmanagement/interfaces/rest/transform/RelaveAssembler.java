package com.chicamax.sentinella.plantmanagement.interfaces.rest.transform;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Relave;
import com.chicamax.sentinella.plantmanagement.domain.model.commands.CreateRelaveCommand;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.CreateRelaveResource;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.RelaveResource;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RelaveAssembler {

    public CreateRelaveCommand toCommand(CreateRelaveResource resource, UUID organizationId, UUID createdByUserId) {
        return new CreateRelaveCommand(
                resource.name(),
                resource.tailingDamId(),
                organizationId,
                createdByUserId,
                resource.capacity(),
                resource.latitude(),
                resource.longitude(),
                resource.address()
        );
    }

    public RelaveResource toResource(Relave relave) {
        return new RelaveResource(
                relave.getId(),
                relave.getName(),
                relave.getTailingDamId(),
                relave.getCapacity(),
                relave.getLatitude(),
                relave.getLongitude(),
                relave.getAddress(),
                relave.getStatus()
        );
    }
}
