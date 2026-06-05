package com.chicamax.sentinella.monitoring.interfaces.rest;

import com.chicamax.sentinella.monitoring.domain.model.queries.GetAllNodesQuery;
import com.chicamax.sentinella.monitoring.domain.services.SensorNodeQueryService;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.SensorNodeResource;
import com.chicamax.sentinella.monitoring.interfaces.rest.transform.SensorNodeAssembler;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/v1/nodes")
public class SensorNodesController {

    private final SensorNodeQueryService sensorNodeQueryService;
    private final SensorNodeAssembler sensorNodeAssembler;
    private final AuthorizationScopeService authorizationScopeService;

    public SensorNodesController(
            SensorNodeQueryService sensorNodeQueryService,
            SensorNodeAssembler sensorNodeAssembler,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.sensorNodeQueryService = sensorNodeQueryService;
        this.sensorNodeAssembler = sensorNodeAssembler;
        this.authorizationScopeService = authorizationScopeService;
    }

    @GetMapping
    public ResponseEntity<List<SensorNodeResource>> getNodes(@AuthenticationPrincipal Jwt jwt) {
        List<SensorNodeResource> nodes = sensorNodeQueryService.handle(new GetAllNodesQuery())
                .stream()
                .filter(node -> authorizationScopeService.canAccessDam(jwt, node.getTailingDamId()))
                .map(sensorNodeAssembler::toResource)
                .toList();
        return ResponseEntity.ok(nodes);
    }

    @GetMapping("/{nodeId}")
    public ResponseEntity<SensorNodeResource> getNode(
            @PathVariable UUID nodeId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var node = sensorNodeQueryService.findById(nodeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nodo no encontrado"));
        if (!authorizationScopeService.canAccessDam(jwt, node.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
        return ResponseEntity.ok(sensorNodeAssembler.toResource(node));
    }
}
