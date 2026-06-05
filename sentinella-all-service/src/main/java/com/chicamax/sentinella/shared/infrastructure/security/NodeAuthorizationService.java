package com.chicamax.sentinella.shared.infrastructure.security;

import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class NodeAuthorizationService {

    private final SensorNodeRepository sensorNodeRepository;
    private final AuthorizationScopeService authorizationScopeService;

    public NodeAuthorizationService(
            SensorNodeRepository sensorNodeRepository,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.sensorNodeRepository = sensorNodeRepository;
        this.authorizationScopeService = authorizationScopeService;
    }

    public boolean canAccessNode(Jwt jwt, UUID nodeId) {
        if (nodeId == null) {
            return false;
        }
        return sensorNodeRepository.findById(nodeId)
                .map(node -> authorizationScopeService.canAccessDam(jwt, node.getTailingDamId()))
                .orElse(false);
    }
}
