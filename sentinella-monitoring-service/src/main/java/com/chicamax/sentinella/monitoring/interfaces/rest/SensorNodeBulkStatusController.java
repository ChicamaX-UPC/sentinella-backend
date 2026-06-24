package com.chicamax.sentinella.monitoring.interfaces.rest;

import com.chicamax.sentinella.monitoring.application.internal.queryservices.SensorNodeBulkStatusQueryService;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.BulkNodeStatusResource;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/nodes")
public class SensorNodeBulkStatusController {

    private final SensorNodeBulkStatusQueryService bulkStatusQueryService;
    private final AuthorizationScopeService authorizationScopeService;

    public SensorNodeBulkStatusController(
            SensorNodeBulkStatusQueryService bulkStatusQueryService,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.bulkStatusQueryService = bulkStatusQueryService;
        this.authorizationScopeService = authorizationScopeService;
    }

    @GetMapping("/bulk-status")
    public ResponseEntity<BulkNodeStatusResource> getBulkStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime since,
            @AuthenticationPrincipal Jwt jwt
    ) {
        boolean scoped = authorizationScopeService.shouldScopeByDam(jwt);
        var damIds = authorizationScopeService.extractDamIds(jwt);
        return ResponseEntity.ok(bulkStatusQueryService.getBulkStatus(scoped, damIds, since));
    }
}
