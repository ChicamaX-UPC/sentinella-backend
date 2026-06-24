package com.chicamax.sentinella.dashboard.infrastructure.integration;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class IamWsTicketClient {

    private final RestClient restClient;
    private final String internalServiceKey;

    public IamWsTicketClient(
            @Value("${sentinella.iam.base-url}") String iamBaseUrl,
            @Value("${sentinella.internal.service-key:}") String internalServiceKey
    ) {
        this.restClient = RestClient.builder().baseUrl(iamBaseUrl).build();
        this.internalServiceKey = internalServiceKey;
    }

    public Optional<WsTicketConsumed> consume(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            return Optional.empty();
        }
        try {
            WsTicketConsumed consumed = restClient.post()
                    .uri("/v1/internal/ws-tickets/consume")
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("ticket", ticket))
                    .retrieve()
                    .body(WsTicketConsumed.class);
            return Optional.ofNullable(consumed);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public record WsTicketConsumed(UUID userId, String role) {
    }
}
