package com.chicamax.sentinella.plantmanagement.application.internal.commandservices;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Relave;
import com.chicamax.sentinella.plantmanagement.domain.model.commands.CreateRelaveCommand;
import com.chicamax.sentinella.plantmanagement.domain.services.RelaveCommandService;
import com.chicamax.sentinella.plantmanagement.infrastructure.messaging.RelaveCreatedRabbitPublisher;
import com.chicamax.sentinella.plantmanagement.infrastructure.persistence.jpa.RelaveRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelaveCommandServiceImpl implements RelaveCommandService {

    private final RelaveRepository relaveRepository;
    private final RelaveCreatedRabbitPublisher relaveCreatedRabbitPublisher;

    public RelaveCommandServiceImpl(
            RelaveRepository relaveRepository,
            RelaveCreatedRabbitPublisher relaveCreatedRabbitPublisher
    ) {
        this.relaveRepository = relaveRepository;
        this.relaveCreatedRabbitPublisher = relaveCreatedRabbitPublisher;
    }

    @Override
    @Transactional
    public Relave create(CreateRelaveCommand command) {
        UUID tailingDamId = command.tailingDamId() != null ? command.tailingDamId() : UUID.randomUUID();
        Relave relave = new Relave(
                UUID.randomUUID(),
                command.name(),
                tailingDamId,
                command.organizationId(),
                command.capacity(),
                command.latitude(),
                command.longitude(),
                command.address()
        );
        Relave saved = relaveRepository.save(relave);
        if (command.createdByUserId() != null) {
            relaveCreatedRabbitPublisher.publish(command.organizationId(), command.createdByUserId(), tailingDamId);
        }
        return saved;
    }
}
