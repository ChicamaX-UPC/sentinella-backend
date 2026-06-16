package com.chicamax.sentinella.iam.interfaces.rest;

import com.chicamax.sentinella.iam.application.internal.commandservices.DeviceTokenCommandService;
import com.chicamax.sentinella.iam.interfaces.rest.resources.RegisterDeviceTokenResource;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users/me/device-tokens")
public class DeviceTokensController {

    private final DeviceTokenCommandService deviceTokenCommandService;

    public DeviceTokensController(DeviceTokenCommandService deviceTokenCommandService) {
        this.deviceTokenCommandService = deviceTokenCommandService;
    }

    @PostMapping
    public ResponseEntity<Void> register(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RegisterDeviceTokenResource resource
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        deviceTokenCommandService.register(userId, resource.token().trim(), resource.platform().trim().toLowerCase());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unregister(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody RegisterDeviceTokenResource resource
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        deviceTokenCommandService.unregister(userId, resource.token().trim());
        return ResponseEntity.noContent().build();
    }
}
