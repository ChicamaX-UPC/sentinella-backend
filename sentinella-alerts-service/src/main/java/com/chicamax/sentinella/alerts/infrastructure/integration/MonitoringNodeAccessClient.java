package com.chicamax.sentinella.alerts.infrastructure.integration;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class MonitoringNodeAccessClient {

    private final RestClient restClient;

    public MonitoringNodeAccessClient(@Value("${sentinella.monitoring.base-url}") String monitoringBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(monitoringBaseUrl).build();
    }

    /** Valida alcance usando el mismo JWT que el llamante (delega en monitoring + AuthorizationScope interno). */
    public boolean canAccessNode(Jwt jwt, UUID nodeId) {
        try {
            restClient.get()
                    .uri("/v1/nodes/{nodeId}", nodeId)
                    .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientResponseException ex) {
            HttpStatusCode code = ex.getStatusCode();
            if (code.equals(HttpStatus.FORBIDDEN) || code.equals(HttpStatus.NOT_FOUND)) {
                return false;
            }
            throw ex;
        }
    }
}
