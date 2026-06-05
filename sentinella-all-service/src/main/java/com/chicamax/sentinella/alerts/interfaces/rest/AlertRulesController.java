package com.chicamax.sentinella.alerts.interfaces.rest;

import com.chicamax.sentinella.alerts.domain.services.AlertRuleCommandService;
import com.chicamax.sentinella.alerts.domain.services.AlertRuleQueryService;
import com.chicamax.sentinella.alerts.interfaces.rest.resources.AlertRuleResource;
import com.chicamax.sentinella.alerts.interfaces.rest.resources.CreateAlertRuleResource;
import com.chicamax.sentinella.alerts.interfaces.rest.resources.UpdateAlertRuleResource;
import com.chicamax.sentinella.alerts.interfaces.rest.transform.AlertRuleAssembler;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/alert-rules")
public class AlertRulesController {

    private final AlertRuleCommandService alertRuleCommandService;
    private final AlertRuleQueryService alertRuleQueryService;
    private final AlertRuleAssembler alertRuleAssembler;

    public AlertRulesController(
            AlertRuleCommandService alertRuleCommandService,
            AlertRuleQueryService alertRuleQueryService,
            AlertRuleAssembler alertRuleAssembler
    ) {
        this.alertRuleCommandService = alertRuleCommandService;
        this.alertRuleQueryService = alertRuleQueryService;
        this.alertRuleAssembler = alertRuleAssembler;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<AlertRuleResource> create(
            @Valid @RequestBody CreateAlertRuleResource resource,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        var created = alertRuleCommandService.create(alertRuleAssembler.toCommand(resource, actorId));
        return ResponseEntity.ok(alertRuleAssembler.toResource(created));
    }

    @GetMapping
    public ResponseEntity<List<AlertRuleResource>> getAll() {
        List<AlertRuleResource> resources = alertRuleQueryService.getAll()
                .stream()
                .map(alertRuleAssembler::toResource)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{ruleId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<AlertRuleResource> update(
            @PathVariable UUID ruleId,
            @Valid @RequestBody UpdateAlertRuleResource resource,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        var updated = alertRuleCommandService.update(alertRuleAssembler.toCommand(ruleId, resource, actorId));
        return ResponseEntity.ok(alertRuleAssembler.toResource(updated));
    }

    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID ruleId) {
        alertRuleCommandService.delete(ruleId);
        return ResponseEntity.noContent().build();
    }
}
