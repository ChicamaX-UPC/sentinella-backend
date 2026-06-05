package com.chicamax.sentinella.nodeadmin.application.internal.commandservices;

import com.chicamax.sentinella.nodeadmin.domain.model.aggregates.IoTNode;
import com.chicamax.sentinella.nodeadmin.domain.model.commands.RegisterNodeCommand;
import com.chicamax.sentinella.nodeadmin.domain.services.IoTNodeCommandService;
import com.chicamax.sentinella.nodeadmin.infrastructure.persistence.jpa.IoTNodeRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IoTNodeCommandServiceImpl implements IoTNodeCommandService {

    private final IoTNodeRepository ioTNodeRepository;

    public IoTNodeCommandServiceImpl(IoTNodeRepository ioTNodeRepository) {
        this.ioTNodeRepository = ioTNodeRepository;
    }

    @Override
    @Transactional
    public IoTNode register(RegisterNodeCommand command) {
        if (ioTNodeRepository.existsByExternalId(command.externalId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El externalId ya existe");
        }
        IoTNode node = new IoTNode(
                UUID.randomUUID(),
                command.externalId(),
                command.name(),
                command.tailingDamId(),
                command.sensorType(),
                command.latitude(),
                command.longitude(),
                command.position3d()
        );
        return ioTNodeRepository.save(node);
    }
}
