package com.chicamax.sentinella.monitoring.interfaces.rest;

import com.chicamax.sentinella.monitoring.domain.services.SensorNodeQueryService;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.InternalNodeScopeResource;
import com.chicamax.sentinella.shared.infrastructure.security.InternalServiceAuth;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/internal/nodes")
public class InternalNodesController {

    private final SensorNodeQueryService sensorNodeQueryService;
    private final SensorNodeRepository sensorNodeRepository;
    private final String internalServiceKey;
    private final boolean requireKey;

    public InternalNodesController(
            SensorNodeQueryService sensorNodeQueryService,
            SensorNodeRepository sensorNodeRepository,
            @Value("${sentinella.internal.service-key:}") String internalServiceKey,
            @Value("${sentinella.internal.require-key:false}") boolean requireKey
    ) {
        this.sensorNodeQueryService = sensorNodeQueryService;
        this.sensorNodeRepository = sensorNodeRepository;
        this.internalServiceKey = internalServiceKey;
        this.requireKey = requireKey;
    }

    @GetMapping("/{nodeId}/scope")
    public ResponseEntity<InternalNodeScopeResource> scope(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @PathVariable UUID nodeId
    ) {
        InternalServiceAuth.require(internalServiceKey, serviceKey, requireKey);
        var node = sensorNodeQueryService.findById(nodeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nodo no encontrado"));
        return ResponseEntity.ok(new InternalNodeScopeResource(node.getTailingDamId()));
    }

    @GetMapping("/by-dams")
    public ResponseEntity<List<UUID>> nodeIdsByDams(
            @RequestHeader(value = "X-Internal-Service-Key", required = false) String serviceKey,
            @RequestParam List<UUID> damIds
    ) {
        InternalServiceAuth.require(internalServiceKey, serviceKey, requireKey);
        if (damIds == null || damIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(sensorNodeRepository.findIdsByTailingDamIdIn(damIds));
    }
}
