package com.chicamax.sentinella.plantmanagement.infrastructure.integration;

import com.chicamax.sentinella.plantmanagement.infrastructure.persistence.jpa.SensorRepository;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import com.chicamax.sentinella.shared.infrastructure.security.SubscriptionStatusClient;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RestSubscriptionStatusClient implements SubscriptionStatusClient {

    private final RestClient restClient;
    private final SensorRepository sensorRepository;
    private final AuthorizationScopeService authorizationScopeService;

    public RestSubscriptionStatusClient(
            @Value("${sentinella.subscriptions.base-url}") String subscriptionsBaseUrl,
            SensorRepository sensorRepository,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.restClient = RestClient.builder().baseUrl(subscriptionsBaseUrl).build();
        this.sensorRepository = sensorRepository;
        this.authorizationScopeService = authorizationScopeService;
    }

    @Override
    public boolean hasActiveSubscription(Jwt jwt) {
        return fetchActive(jwt) != null;
    }

    @Override
    public SubscriptionQuota quotaFor(Jwt jwt) {
        ActiveSubscription active = fetchActive(jwt);
        int currentNodes = countSensors(jwt);
        if (active == null) {
            return new SubscriptionQuota(false, 0, currentNodes);
        }
        return new SubscriptionQuota(true, active.sensorLimit(), currentNodes);
    }

    private ActiveSubscription fetchActive(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        try {
            var response = restClient.get()
                    .uri("/v1/subscriptions/active")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue())
                    .retrieve()
                    .toEntity(ActiveSubscription.class);
            if (response.getStatusCode().value() == HttpStatus.NO_CONTENT.value() || response.getBody() == null) {
                return null;
            }
            return response.getBody();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()
                    || ex.getStatusCode().value() == HttpStatus.NO_CONTENT.value()) {
                return null;
            }
            throw ex;
        }
    }

    private int countSensors(Jwt jwt) {
        Set<UUID> damIds = authorizationScopeService.extractDamIds(jwt);
        if (damIds.isEmpty()) {
            return 0;
        }
        return Math.toIntExact(sensorRepository.countByTailingDamIdIn(damIds));
    }

    private record ActiveSubscription(UUID id, int sensorLimit, String status) {
    }
}
