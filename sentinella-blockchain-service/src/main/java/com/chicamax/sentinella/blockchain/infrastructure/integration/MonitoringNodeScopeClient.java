package com.chicamax.sentinella.blockchain.infrastructure.integration;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MonitoringNodeScopeClient {

    private final RestClient restClient;
    private final String internalServiceKey;

    public MonitoringNodeScopeClient(
            @Value("${sentinella.monitoring.base-url}") String monitoringBaseUrl,
            @Value("${sentinella.internal.service-key:}") String internalServiceKey
    ) {
        this.restClient = RestClient.builder().baseUrl(monitoringBaseUrl).build();
        this.internalServiceKey = internalServiceKey;
    }

    public List<UUID> resolveNodeIds(Collection<UUID> damIds) {
        if (damIds == null || damIds.isEmpty()) {
            return List.of();
        }
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/internal/nodes/by-dams")
                        .queryParam("damIds", damIds.toArray())
                        .build())
                .header("X-Internal-Service-Key", internalServiceKey)
                .retrieve()
                .body(new ParameterizedTypeReference<List<UUID>>() {
                });
    }
}
