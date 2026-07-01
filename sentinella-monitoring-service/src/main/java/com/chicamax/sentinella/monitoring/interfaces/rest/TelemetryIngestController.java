package com.chicamax.sentinella.monitoring.interfaces.rest;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorReadingReceivedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ingesta HTTP de telemetría desde la capa edge (sentinella-edge → edge-gw → gateway).
 *
 * <p>El gateway enruta {@code POST /api/v1/monitoring/readings/ingest} (StripPrefix=1) a este
 * endpoint. La petición se republica al exchange {@code sentinella} con routing key
 * {@code telemetry}, de modo que la consume el {@code TelemetryIngestionConsumer} ya existente y
 * recorre el mismo pipeline (persistencia + evaluación de umbrales) que la ingesta por AMQP.
 */
@RestController
@RequestMapping("/v1/monitoring/readings")
public class TelemetryIngestController {

    private final RabbitTemplate rabbitTemplate;

    public TelemetryIngestController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Void> ingest(@RequestBody SensorReadingReceivedMessage message) {
        rabbitTemplate.convertAndSend(
                SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                SentinellaMessagingConstants.TELEMETRY_ROUTING,
                message
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
