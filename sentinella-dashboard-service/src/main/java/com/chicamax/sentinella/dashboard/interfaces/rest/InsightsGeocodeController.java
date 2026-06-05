package com.chicamax.sentinella.dashboard.interfaces.rest;

import com.chicamax.sentinella.shared.infrastructure.maps.GoogleMapsClient;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/v1/dashboard")
public class InsightsGeocodeController {

    private final ObjectProvider<GoogleMapsClient> googleMapsClient;

    public InsightsGeocodeController(ObjectProvider<GoogleMapsClient> googleMapsClient) {
        this.googleMapsClient = googleMapsClient;
    }

    @GetMapping("/geocode")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','READ_ONLY')")
    public ResponseEntity<Map<String, BigDecimal>> geocode(@RequestParam String address) {
        GoogleMapsClient client = googleMapsClient.getIfAvailable();
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Google Maps API no configurada");
        }
        Optional<GoogleMapsClient.GeoPoint> point = client.geocode(address);
        return point.map(p -> ResponseEntity.ok(Map.of("latitude", p.latitude(), "longitude", p.longitude())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
