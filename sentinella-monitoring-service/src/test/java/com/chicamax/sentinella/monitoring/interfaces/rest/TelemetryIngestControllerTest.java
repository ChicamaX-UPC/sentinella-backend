package com.chicamax.sentinella.monitoring.interfaces.rest;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.SensorReadingReceivedMessage;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TelemetryIngestController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "sentinella.internal.service-key=test-internal-key",
        "sentinella.internal.require-key=true"
})
class TelemetryIngestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldRejectIngestWithoutServiceKey() throws Exception {
        mockMvc.perform(post("/v1/internal/telemetry/ingest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nodeId": "c1000001-0001-4001-8001-000000000001",
                                  "sensorType": "water_level",
                                  "value": 90.0,
                                  "timestamp": "2026-07-01T12:00:00Z"
                                }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void shouldAcceptIngestWithServiceKey() throws Exception {
        mockMvc.perform(post("/v1/internal/telemetry/ingest")
                        .header("X-Internal-Service-Key", "test-internal-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nodeId": "c1000001-0001-4001-8001-000000000001",
                                  "sensorType": "water_level",
                                  "value": 90.0,
                                  "timestamp": "2026-07-01T12:00:00Z"
                                }
                                """))
                .andExpect(status().isAccepted());

        verify(rabbitTemplate).convertAndSend(
                eq(SentinellaMessagingConstants.SENTINELLA_EXCHANGE),
                eq(SentinellaMessagingConstants.TELEMETRY_ROUTING),
                org.mockito.ArgumentMatchers.any(SensorReadingReceivedMessage.class)
        );
    }
}
