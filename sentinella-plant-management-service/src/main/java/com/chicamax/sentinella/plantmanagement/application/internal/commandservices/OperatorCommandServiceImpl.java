package com.chicamax.sentinella.plantmanagement.application.internal.commandservices;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Operator;
import com.chicamax.sentinella.plantmanagement.domain.model.commands.RegisterOperatorCommand;
import com.chicamax.sentinella.plantmanagement.domain.services.OperatorCommandService;
import com.chicamax.sentinella.plantmanagement.infrastructure.persistence.jpa.OperatorRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OperatorCommandServiceImpl implements OperatorCommandService {

    private final OperatorRepository operatorRepository;

    public OperatorCommandServiceImpl(OperatorRepository operatorRepository) {
        this.operatorRepository = operatorRepository;
    }

    @Override
    @Transactional
    public Operator register(RegisterOperatorCommand command) {
        if (operatorRepository.existsByEmail(command.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }
        Operator operator = new Operator(
                UUID.randomUUID(),
                command.ownerId(),
                command.fullName(),
                command.email()
        );
        return operatorRepository.save(operator);
    }
}
