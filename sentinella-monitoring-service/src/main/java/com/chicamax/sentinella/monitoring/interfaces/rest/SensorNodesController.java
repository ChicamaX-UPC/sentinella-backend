package com.chicamax.sentinella.monitoring.interfaces.rest;

import com.chicamax.sentinella.monitoring.domain.model.queries.GetAllNodesQuery;
import com.chicamax.sentinella.monitoring.domain.services.SensorNodeQueryService;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.SensorNodeResource;
import com.chicamax.sentinella.monitoring.interfaces.rest.transform.SensorNodeAssembler;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import com.chicamax.sentinella.shared.interfaces.rest.PageResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<PageResponse<SensorNodeResource>> getNodes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int limit,
            @AuthenticationPrincipal Jwt jwt
    ) {
        boolean scoped = authorizationScopeService.shouldScopeByDam(jwt);
        var damIds = authorizationScopeService.extractDamIds(jwt);
        var result = sensorNodeQueryService.handle(new GetAllNodesQuery(page, limit, scoped, damIds));
        var content = result.getContent().stream().map(sensorNodeAssembler::toResource).toList();
        return ResponseEntity.ok(PageResponse.of(content, result.getNumber(), result.getSize(), result.getTotalElements()));
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
