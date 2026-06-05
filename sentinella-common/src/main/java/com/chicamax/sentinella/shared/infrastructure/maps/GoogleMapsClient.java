package com.chicamax.sentinella.shared.infrastructure.maps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "sentinella.google-maps.api-key")
public class GoogleMapsClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GoogleMapsClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${sentinella.google-maps.api-key}") String apiKey
    ) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    public Optional<GeoPoint> geocode(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }
        String encoded = URLEncoder.encode(address.trim(), StandardCharsets.UTF_8);
        URI uri = URI.create(
                "https://maps.googleapis.com/maps/api/geocode/json?address=" + encoded + "&key=" + apiKey
        );
        String body = restClient.get().uri(uri).retrieve().body(String.class);
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode location = root.path("results").path(0).path("geometry").path("location");
            if (location.isMissingNode()) {
                return Optional.empty();
            }
            return Optional.of(new GeoPoint(
                    BigDecimal.valueOf(location.path("lat").asDouble()),
                    BigDecimal.valueOf(location.path("lng").asDouble())
            ));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public record GeoPoint(BigDecimal latitude, BigDecimal longitude) {
    }
}
