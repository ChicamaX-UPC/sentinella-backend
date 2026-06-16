package com.chicamax.sentinella.alerts.infrastructure.integration;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IamPushTargetClient {

    private final RestClient restClient;
    private final String internalServiceKey;

    public IamPushTargetClient(
            @Value("${sentinella.iam.base-url}") String iamBaseUrl,
            @Value("${sentinella.internal.service-key:}") String internalServiceKey
    ) {
        this.restClient = RestClient.builder().baseUrl(iamBaseUrl).build();
        this.internalServiceKey = internalServiceKey;
    }

    public List<PushTarget> findTargets(UUID tailingDamId) {
        if (tailingDamId == null) {
            return List.of();
        }
        PushTarget[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/internal/push-targets")
                        .queryParam("tailingDamId", tailingDamId)
                        .build())
                .header("X-Internal-Service-Key", internalServiceKey)
                .retrieve()
                .body(PushTarget[].class);
        return response == null ? List.of() : List.of(response);
    }

    public record PushTarget(String token, String platform) {
    }
}
