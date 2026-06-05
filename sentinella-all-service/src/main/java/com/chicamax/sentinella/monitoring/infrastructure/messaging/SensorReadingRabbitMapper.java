package com.chicamax.sentinella.monitoring.infrastructure.messaging;

import com.chicamax.sentinella.monitoring.domain.model.commands.RegisterSensorReadingCommand;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorStatus;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorReadingReceivedMessage;
import org.springframework.stereotype.Component;

@Component
public class SensorReadingRabbitMapper {

    public RegisterSensorReadingCommand toCommand(SensorReadingReceivedMessage m) {
        return new RegisterSensorReadingCommand(
                m.nodeId(),
                m.timestamp(),
                parseSensorType(m.sensorType()),
                m.value(),
                m.unit() != null && !m.unit().isBlank() ? m.unit() : "—",
                parseSensorStatus(m.status()),
                m.rawPayload()
        );
    }

    private static SensorType parseSensorType(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("sensorType requerido");
        }
        String n = raw.trim().toLowerCase().replace('-', '_');
        return switch (n) {
            case "water_level" -> SensorType.WATER_LEVEL;
            case "pressure" -> SensorType.PRESSURE;
            case "inclination" -> SensorType.INCLINATION;
            case "ph" -> SensorType.PH;
            case "turbidity" -> SensorType.TURBIDITY;
            case "pluviometer" -> SensorType.PLUVIOMETER;
            default -> {
                for (SensorType t : SensorType.values()) {
                    if (t.name().equalsIgnoreCase(n)) {
                        yield t;
                    }
                }
                throw new IllegalArgumentException("sensorType no soportado: " + raw);
            }
        };
    }

    private static SensorStatus parseSensorStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return SensorStatus.OK;
        }
        return SensorStatus.valueOf(raw.trim().toUpperCase().replace(' ', '_'));
    }
}
