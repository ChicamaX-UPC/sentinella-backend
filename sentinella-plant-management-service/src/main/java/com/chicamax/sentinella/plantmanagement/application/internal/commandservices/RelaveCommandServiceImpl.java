package com.chicamax.sentinella.plantmanagement.application.internal.commandservices;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Relave;
import com.chicamax.sentinella.plantmanagement.domain.model.commands.CreateRelaveCommand;
import com.chicamax.sentinella.plantmanagement.domain.services.RelaveCommandService;
import com.chicamax.sentinella.plantmanagement.infrastructure.persistence.jpa.RelaveRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelaveCommandServiceImpl implements RelaveCommandService {

    private final RelaveRepository relaveRepository;

    public RelaveCommandServiceImpl(RelaveRepository relaveRepository) {
        this.relaveRepository = relaveRepository;
    }

    @Override
    @Transactional
    public Relave create(CreateRelaveCommand command) {
        Relave relave = new Relave(
                UUID.randomUUID(),
                command.name(),
                command.tailingDamId(),
                command.capacity(),
                command.latitude(),
                command.longitude(),
                command.address()
        );
        return relaveRepository.save(relave);
    }
}
