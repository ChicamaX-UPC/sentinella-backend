package com.chicamax.sentinella.monitoring.interfaces.rest;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorReadingReceivedMessage;
import com.chicamax.sentinella.shared.infrastructure.security.InternalServiceAuth;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ingesta HTTP de telemetría desde la capa edge (sentinella-edge → edge-gw → gateway).
 *
 * <p>El gateway enruta {@code POST /api/v1/monitoring/readings/ingest} hacia
 * {@code /v1/internal/telemetry/ingest}. La petición se republica al exchange {@code sentinella} con routing key
 * {@code telemetry}, de modo que la consume el {@code TelemetryIngestionConsumer} ya existente y
 * recorre el mismo pipeline (persistencia + evaluación de umbrales) que la ingesta por AMQP.
 *
 * <p>Protegido con {@link InternalServiceAuth} ({@code X-Internal-Service-Key}), igual que los
 * endpoints {@code /v1/internal/**}; Spring Security deja pasar la ruta sin JWT.
 */
@RestController
@RequestMapping("/v1/internal/telemetry")
public class TelemetryIngestController {

    private final RabbitTemplate rabbitTemplate;
    private final String internalServiceKey;
    private final boolean requireKey;

    public TelemetryIngestController(
            RabbitTemplate rabbitTemplate,
            @Value("${sentinella.internal.service-key:}") String internalServiceKey,
            @Value("${sentinella.internal.require-key:false}") boolean requireKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.internalServiceKey = internalServiceKey;
        this.requireKey = requireKey;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Void> ingest(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @RequestBody SensorReadingReceivedMessage message
    ) {
        InternalServiceAuth.require(internalServiceKey, serviceKey, requireKey);
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.TELEMETRY_ROUTING,
                message
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
