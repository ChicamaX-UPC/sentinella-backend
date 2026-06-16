package com.chicamax.sentinella.monitoring.interfaces.rest;

import com.chicamax.sentinella.monitoring.domain.services.SensorNodeQueryService;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.InternalNodeScopeResource;
import com.chicamax.sentinella.shared.infrastructure.security.InternalServiceAuth;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/internal/nodes")
public class InternalNodesController {

    private final SensorNodeQueryService sensorNodeQueryService;
    private final String internalServiceKey;

    public InternalNodesController(
            SensorNodeQueryService sensorNodeQueryService,
            @Value("${sentinella.internal.service-key:}") String internalServiceKey
    ) {
        this.sensorNodeQueryService = sensorNodeQueryService;
        this.internalServiceKey = internalServiceKey;
    }

    @GetMapping("/{nodeId}/scope")
    public ResponseEntity<InternalNodeScopeResource> scope(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @PathVariable UUID nodeId
    ) {
        InternalServiceAuth.require(internalServiceKey, serviceKey);
        var node = sensorNodeQueryService.findById(nodeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nodo no encontrado"));
        return ResponseEntity.ok(new InternalNodeScopeResource(node.getTailingDamId()));
    }
}
