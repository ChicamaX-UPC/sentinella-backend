package com.chicamax.sentinella.alerts.infrastructure.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AlertScopeService {

    private final RestClient monitoring;

    public AlertScopeService(@Value("${sentinella.monitoring.base-url}") String monitoringBaseUrl) {
        this.monitoring = RestClient.builder().baseUrl(monitoringBaseUrl).build();
    }

    public Set<UUID> resolveAllowedNodeIds(Jwt jwt) {
        return resolveNodeIdsForDams(jwt, Set.of());
    }

    public Set<UUID> resolveNodeIdsForDams(Jwt jwt, Set<UUID> damIds) {
        Set<UUID> scoped = new HashSet<>();
        int page = 0;
        while (true) {
            int currentPage = page;
            WirePage response = monitoring.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/nodes")
                            .queryParam("page", currentPage)
                            .queryParam("limit", 200)
                            .build())
                    .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<WirePage>() {});
            if (response == null || response.content() == null || response.content().isEmpty()) {
                break;
            }
            for (WireNode node : response.content()) {
                if (damIds == null || damIds.isEmpty() || damIds.contains(node.tailingDamId())) {
                    scoped.add(node.id());
                }
            }
            if (response.last()) {
                break;
            }
            page++;
        }
        return scoped;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WireNode(UUID id, UUID tailingDamId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WirePage(List<WireNode> content, boolean last) {
    }
}
