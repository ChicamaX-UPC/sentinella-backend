package com.chicamax.sentinella.alerts.infrastructure.integration;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class MonitoringInternalClient {

    private final RestClient restClient;
    private final String internalServiceKey;

    public MonitoringInternalClient(
            @Value("${sentinella.monitoring.base-url}") String monitoringBaseUrl,
            @Value("${sentinella.internal.service-key:}") String internalServiceKey
    ) {
        this.restClient = RestClient.builder().baseUrl(monitoringBaseUrl).build();
        this.internalServiceKey = internalServiceKey;
    }

    public UUID resolveTailingDamId(UUID nodeId) {
        try {
            ScopeResponse response = restClient.get()
                    .uri("/v1/internal/nodes/{nodeId}/scope", nodeId)
                    .header("X-Internal-Service-Key", internalServiceKey)
                    .retrieve()
                    .body(ScopeResponse.class);
            return response == null ? null : response.tailingDamId();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }
            throw ex;
        }
    }

    private record ScopeResponse(UUID tailingDamId) {
    }
}
