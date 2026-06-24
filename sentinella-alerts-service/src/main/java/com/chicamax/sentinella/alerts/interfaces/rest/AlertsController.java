package com.chicamax.sentinella.alerts.interfaces.rest;

import com.chicamax.sentinella.alerts.domain.model.commands.UpdateAlertCommand;
import com.chicamax.sentinella.alerts.domain.model.queries.GetActiveAlertsQuery;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertAction;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import com.chicamax.sentinella.alerts.domain.services.AlertCommandService;
import com.chicamax.sentinella.alerts.domain.services.AlertQueryService;
import com.chicamax.sentinella.alerts.interfaces.rest.resources.AlertAuditResource;
import com.chicamax.sentinella.alerts.interfaces.rest.resources.AlertResource;
import com.chicamax.sentinella.alerts.interfaces.rest.resources.UpdateAlertStatusResource;
import com.chicamax.sentinella.alerts.interfaces.rest.transform.AlertAssembler;
import com.chicamax.sentinella.alerts.infrastructure.integration.AlertScopeService;
import com.chicamax.sentinella.alerts.infrastructure.integration.MonitoringNodeAccessClient;
import com.chicamax.sentinella.shared.interfaces.rest.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/v1/alerts")
public class AlertsController {

    private final AlertCommandService alertCommandService;
    private final AlertQueryService alertQueryService;
    private final AlertAssembler alertAssembler;
    private final MonitoringNodeAccessClient monitoringNodeAccessClient;
    private final AlertScopeService alertScopeService;

    public AlertsController(
            AlertCommandService alertCommandService,
            AlertQueryService alertQueryService,
            AlertAssembler alertAssembler,
            MonitoringNodeAccessClient monitoringNodeAccessClient,
            AlertScopeService alertScopeService
    ) {
        this.alertCommandService = alertCommandService;
        this.alertQueryService = alertQueryService;
        this.alertAssembler = alertAssembler;
        this.monitoringNodeAccessClient = monitoringNodeAccessClient;
        this.alertScopeService = alertScopeService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AlertResource>> getAlerts(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false) UUID nodeId,
            @RequestParam(required = false) String damIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (nodeId != null && !monitoringNodeAccessClient.canAccessNode(jwt, nodeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al nodo solicitado");
        }
        Set<UUID> scopedNodeIds = alertScopeService.resolveNodeIdsForDams(jwt, parseDamIds(damIds));
        var result = alertQueryService.handle(new GetActiveAlertsQuery(
                status,
                severity,
                nodeId,
                page,
                limit,
                true,
                scopedNodeIds
        ));
        var content = result.getContent().stream()
                .map(alertAssembler::toResource)
                .toList();
        return ResponseEntity.ok(PageResponse.of(content, result.getNumber(), result.getSize(), result.getTotalElements()));
    }

    private static Set<UUID> parseDamIds(String damIds) {
        if (damIds == null || damIds.isBlank()) {
            return Set.of();
        }
        return java.util.Arrays.stream(damIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(UUID::fromString)
                .collect(java.util.stream.Collectors.toSet());
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<AlertResource> getAlert(
            @PathVariable UUID alertId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var alert = alertQueryService.findById(alertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta no encontrada"));
        if (!monitoringNodeAccessClient.canAccessNode(jwt, alert.getNodeId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al nodo solicitado");
        }
        return ResponseEntity.ok(alertAssembler.toResource(alert));
    }

    @PatchMapping("/{alertId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','PLANT_MANAGER','FIELD_OPERATOR')")
    public ResponseEntity<AlertResource> updateAlert(
            @PathVariable UUID alertId,
            @Valid @RequestBody UpdateAlertStatusResource resource,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        String actorRole = jwt.getClaimAsString("role");
        if ("FIELD_OPERATOR".equals(actorRole) && resource.action() != AlertAction.ACKNOWLEDGE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "FIELD_OPERATOR solo puede confirmar alertas");
        }
        var existing = alertQueryService.findById(alertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta no encontrada"));
        if (!monitoringNodeAccessClient.canAccessNode(jwt, existing.getNodeId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al nodo solicitado");
        }
        var updated = alertCommandService.update(new UpdateAlertCommand(
                alertId,
                resource.action(),
                actorId,
                actorRole,
                resource.assignedTo(),
                mergeAckNotes(resource)
        ));
        return ResponseEntity.ok(alertAssembler.toResource(updated));
    }

    private static String mergeAckNotes(UpdateAlertStatusResource resource) {
        String notes = resource.notes();
        if (resource.latitude() == null || resource.longitude() == null) {
            return notes;
        }
        String geo = String.format(
                "Ubicación ACK: %.5f, %.5f",
                resource.latitude(),
                resource.longitude()
        );
        if (notes == null || notes.isBlank()) {
            return geo;
        }
        return notes.trim() + " | " + geo;
    }

    @GetMapping("/{alertId}/audit")
    public ResponseEntity<List<AlertAuditResource>> getAudit(
            @PathVariable UUID alertId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var alert = alertQueryService.findById(alertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerta no encontrada"));
        if (!monitoringNodeAccessClient.canAccessNode(jwt, alert.getNodeId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al nodo solicitado");
        }
        List<AlertAuditResource> audit = alertQueryService.getAuditLog(alertId).stream()
                .map(alertAssembler::toAuditResource)
                .toList();
        return ResponseEntity.ok(audit);
    }
}
