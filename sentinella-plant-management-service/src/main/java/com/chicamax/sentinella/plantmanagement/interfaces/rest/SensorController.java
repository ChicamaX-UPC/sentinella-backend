package com.chicamax.sentinella.plantmanagement.interfaces.rest;

import com.chicamax.sentinella.plantmanagement.domain.services.SensorCommandService;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.NodeResource;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.resources.RegisterNodeResource;
import com.chicamax.sentinella.plantmanagement.interfaces.rest.transform.SensorAssembler;
import com.chicamax.sentinella.shared.infrastructure.security.SubscriptionGuard;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/nodes")
public class SensorController {

    private final SensorCommandService sensorCommandService;
    private final SensorAssembler sensorAssembler;
    private final SubscriptionGuard subscriptionGuard;

    public SensorController(
            SensorCommandService sensorCommandService,
            SensorAssembler sensorAssembler,
            SubscriptionGuard subscriptionGuard
    ) {
        this.sensorCommandService = sensorCommandService;
        this.sensorAssembler = sensorAssembler;
        this.subscriptionGuard = subscriptionGuard;
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<NodeResource> register(
            @Valid @RequestBody RegisterNodeResource resource,
            @AuthenticationPrincipal Jwt jwt
    ) {
        subscriptionGuard.requireActiveSubscription(jwt);
        subscriptionGuard.requireSensorCapacity(jwt, 1);
        UUID ownerUserId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        var created = sensorCommandService.register(sensorAssembler.toCommand(resource, ownerUserId));
        return ResponseEntity.ok(sensorAssembler.toResource(created));
    }
}
