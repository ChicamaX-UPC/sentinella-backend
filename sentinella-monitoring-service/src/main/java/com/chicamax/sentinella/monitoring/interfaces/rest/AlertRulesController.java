package com.chicamax.sentinella.monitoring.interfaces.rest;

import com.chicamax.sentinella.monitoring.domain.services.ThresholdRuleCommandService;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ThresholdRuleRepository;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.AlertRuleResource;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.CreateAlertRuleResource;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.UpdateAlertRuleResource;
import com.chicamax.sentinella.monitoring.interfaces.rest.transform.AlertRuleAssembler;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import com.chicamax.sentinella.shared.interfaces.rest.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/alert-rules")
public class AlertRulesController {

    private final ThresholdRuleCommandService thresholdRuleCommandService;
    private final AlertRuleAssembler alertRuleAssembler;
    private final ThresholdRuleRepository thresholdRuleRepository;
    private final SensorNodeRepository sensorNodeRepository;
    private final AuthorizationScopeService authorizationScopeService;

    public AlertRulesController(
            ThresholdRuleCommandService thresholdRuleCommandService,
            AlertRuleAssembler alertRuleAssembler,
            ThresholdRuleRepository thresholdRuleRepository,
            SensorNodeRepository sensorNodeRepository,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.thresholdRuleCommandService = thresholdRuleCommandService;
        this.alertRuleAssembler = alertRuleAssembler;
        this.thresholdRuleRepository = thresholdRuleRepository;
        this.sensorNodeRepository = sensorNodeRepository;
        this.authorizationScopeService = authorizationScopeService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<AlertRuleResource> create(
            @Valid @RequestBody CreateAlertRuleResource resource,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        var created = thresholdRuleCommandService.create(alertRuleAssembler.toCommand(resource, actorId));
        return ResponseEntity.ok(alertRuleAssembler.toResource(created));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR','READ_ONLY')")
    public ResponseEntity<PageResponse<AlertRuleResource>> getAll(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int limit
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        PageRequest pageable = PageRequest.of(Math.max(page, 0), safeLimit);
        Set<UUID> damIds = authorizationScopeService.extractDamIds(jwt);
        Page<com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule> result;
        if (damIds.isEmpty()) {
            result = Page.empty(pageable);
        } else {
            List<UUID> nodeIds = sensorNodeRepository.findIdsByTailingDamIdIn(damIds);
            if (nodeIds.isEmpty()) {
                result = Page.empty(pageable);
            } else {
                result = thresholdRuleRepository.findByNodeIdIn(nodeIds, pageable);
            }
        }
        var content = result.getContent().stream().map(alertRuleAssembler::toResource).toList();
        return ResponseEntity.ok(PageResponse.of(content, result.getNumber(), result.getSize(), result.getTotalElements()));
    }

    @PutMapping("/{ruleId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<AlertRuleResource> update(
            @PathVariable UUID ruleId,
            @Valid @RequestBody UpdateAlertRuleResource resource,
            @AuthenticationPrincipal Jwt jwt
    ) {
        requireRuleAccess(jwt, ruleId);
        UUID actorId = UUID.fromString(jwt.getSubject());
        var updated = thresholdRuleCommandService.update(alertRuleAssembler.toCommand(ruleId, resource, actorId));
        return ResponseEntity.ok(alertRuleAssembler.toResource(updated));
    }

    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID ruleId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        requireRuleAccess(jwt, ruleId);
        thresholdRuleCommandService.delete(ruleId);
        return ResponseEntity.noContent().build();
    }

    private void requireRuleAccess(Jwt jwt, UUID ruleId) {
        var rule = thresholdRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla no encontrada"));
        Set<UUID> damIds = authorizationScopeService.extractDamIds(jwt);
        if (damIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso a la regla solicitada");
        }
        List<UUID> nodeIds = sensorNodeRepository.findIdsByTailingDamIdIn(damIds);
        if (!nodeIds.contains(rule.getNodeId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso a la regla solicitada");
        }
    }
}
