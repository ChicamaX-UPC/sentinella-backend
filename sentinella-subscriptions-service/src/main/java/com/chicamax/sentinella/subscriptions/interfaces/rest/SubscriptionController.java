package com.chicamax.sentinella.subscriptions.interfaces.rest;

import com.chicamax.sentinella.subscriptions.domain.services.SubscriptionCommandService;
import com.chicamax.sentinella.subscriptions.interfaces.rest.resources.SubscriptionResource;
import com.chicamax.sentinella.subscriptions.interfaces.rest.resources.SubscriptionStatusResource;
import com.chicamax.sentinella.subscriptions.interfaces.rest.transform.SubscriptionResourceAssembler;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionResourceAssembler subscriptionResourceAssembler;

    public SubscriptionController(
            SubscriptionCommandService subscriptionCommandService,
            SubscriptionResourceAssembler subscriptionResourceAssembler
    ) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionResourceAssembler = subscriptionResourceAssembler;
    }

    @GetMapping("/active")
    public ResponseEntity<SubscriptionResource> getActive(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return subscriptionCommandService.findActiveByUserId(userId)
                .map(subscription -> ResponseEntity.ok(subscriptionResourceAssembler.toResource(subscription)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/status")
    public ResponseEntity<SubscriptionStatusResource> getStatus(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        SubscriptionResource resource = subscriptionCommandService.findActiveByUserId(userId)
                .map(subscriptionResourceAssembler::toResource)
                .orElse(null);
        return ResponseEntity.ok(new SubscriptionStatusResource(resource != null, resource));
    }
}
