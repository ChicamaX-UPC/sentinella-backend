package com.chicamax.sentinella.iam.interfaces.rest;

import com.chicamax.sentinella.iam.application.internal.commandservices.DeviceTokenCommandService;
import com.chicamax.sentinella.iam.interfaces.rest.resources.PushTargetResource;
import com.chicamax.sentinella.shared.infrastructure.security.InternalServiceAuth;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/internal/push-targets")
public class InternalPushTargetsController {

    private final DeviceTokenCommandService deviceTokenCommandService;
    private final String internalServiceKey;
    private final boolean requireKey;

    public InternalPushTargetsController(
            DeviceTokenCommandService deviceTokenCommandService,
            @Value("${sentinella.internal.service-key:}") String internalServiceKey,
            @Value("${sentinella.internal.require-key:false}") boolean requireKey
    ) {
        this.deviceTokenCommandService = deviceTokenCommandService;
        this.internalServiceKey = internalServiceKey;
        this.requireKey = requireKey;
    }

    @GetMapping
    public ResponseEntity<List<PushTargetResource>> listForDam(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @RequestParam UUID tailingDamId
    ) {
        InternalServiceAuth.require(internalServiceKey, serviceKey, requireKey);
        List<PushTargetResource> targets = deviceTokenCommandService.findPushTargets(tailingDamId).stream()
                .map(token -> new PushTargetResource(token.getToken(), token.getPlatform()))
                .toList();
        return ResponseEntity.ok(targets);
    }
}
