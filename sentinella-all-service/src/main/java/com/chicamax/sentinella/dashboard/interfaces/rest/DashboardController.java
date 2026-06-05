package com.chicamax.sentinella.dashboard.interfaces.rest;

import com.chicamax.sentinella.dashboard.domain.model.queries.GetExecutiveDashboardQuery;
import com.chicamax.sentinella.dashboard.domain.model.queries.GetFieldDashboardQuery;
import com.chicamax.sentinella.dashboard.domain.model.queries.GetNodesMapQuery;
import com.chicamax.sentinella.dashboard.domain.services.DashboardQueryService;
import com.chicamax.sentinella.dashboard.interfaces.rest.resources.ExecutiveDashboardResource;
import com.chicamax.sentinella.dashboard.interfaces.rest.resources.FieldDashboardResource;
import com.chicamax.sentinella.dashboard.interfaces.rest.resources.NodesMapResource;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/dashboard")
public class DashboardController {

    private final DashboardQueryService dashboardQueryService;
    private final AuthorizationScopeService authorizationScopeService;

    public DashboardController(
            DashboardQueryService dashboardQueryService,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.dashboardQueryService = dashboardQueryService;
        this.authorizationScopeService = authorizationScopeService;
    }

    @GetMapping("/executive")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','READ_ONLY')")
    public ResponseEntity<ExecutiveDashboardResource> executive(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(dashboardQueryService.getExecutive(
                new GetExecutiveDashboardQuery(authorizationScopeService.extractDamIds(jwt))
        ));
    }

    @GetMapping("/field")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR')")
    public ResponseEntity<FieldDashboardResource> field(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(dashboardQueryService.getField(
                new GetFieldDashboardQuery(userId, authorizationScopeService.extractDamIds(jwt))
        ));
    }

    @GetMapping("/nodes-map")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','READ_ONLY')")
    public ResponseEntity<NodesMapResource> nodesMap(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(dashboardQueryService.getNodesMap(
                new GetNodesMapQuery(authorizationScopeService.extractDamIds(jwt))
        ));
    }
}
