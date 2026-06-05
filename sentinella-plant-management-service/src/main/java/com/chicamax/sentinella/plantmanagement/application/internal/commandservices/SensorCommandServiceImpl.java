package com.chicamax.sentinella.plantmanagement.application.internal.commandservices;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Sensor;
import com.chicamax.sentinella.plantmanagement.domain.model.commands.RegisterSensorCommand;
import com.chicamax.sentinella.plantmanagement.domain.services.SensorCommandService;
import com.chicamax.sentinella.plantmanagement.infrastructure.messaging.SensorRegisteredRabbitPublisher;
import com.chicamax.sentinella.plantmanagement.infrastructure.persistence.jpa.SensorRepository;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorRegisteredMessage;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SensorCommandServiceImpl implements SensorCommandService {

    private final SensorRepository sensorRepository;
    private final SensorRegisteredRabbitPublisher sensorRegisteredRabbitPublisher;

    public SensorCommandServiceImpl(
            SensorRepository sensorRepository,
            SensorRegisteredRabbitPublisher sensorRegisteredRabbitPublisher
    ) {
        this.sensorRepository = sensorRepository;
        this.sensorRegisteredRabbitPublisher = sensorRegisteredRabbitPublisher;
    }

    @Override
    @Transactional
    public Sensor register(RegisterSensorCommand command) {
        if (sensorRepository.existsByExternalId(command.externalId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El externalId ya existe");
        }
        Sensor sensor = new Sensor(
                UUID.randomUUID(),
                command.externalId(),
                command.name(),
                command.tailingDamId(),
                command.sensorType(),
                command.latitude(),
                command.longitude(),
                command.position3d()
        );
        Sensor saved = sensorRepository.save(sensor);
        UUID ownerUserId = command.ownerUserId() != null ? command.ownerUserId() : saved.getId();
        sensorRegisteredRabbitPublisher.publish(
                new SensorRegisteredMessage(saved.getId(), saved.getId(), ownerUserId, saved.getSensorType())
        );
        return saved;
    }
}
