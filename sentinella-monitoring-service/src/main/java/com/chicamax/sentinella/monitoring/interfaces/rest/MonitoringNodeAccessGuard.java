package com.chicamax.sentinella.monitoring.interfaces.rest;

import com.chicamax.sentinella.monitoring.domain.services.SensorNodeQueryService;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class MonitoringNodeAccessGuard {

    private final SensorNodeQueryService sensorNodeQueryService;
    private final AuthorizationScopeService authorizationScopeService;

    public MonitoringNodeAccessGuard(
            SensorNodeQueryService sensorNodeQueryService,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.sensorNodeQueryService = sensorNodeQueryService;
        this.authorizationScopeService = authorizationScopeService;
    }

    public void ensureCanAccess(Jwt jwt, UUID nodeId) {
        var node = sensorNodeQueryService.findById(nodeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nodo no encontrado"));
        if (!authorizationScopeService.canAccessDam(jwt, node.getTailingDamId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin acceso al tranque solicitado");
        }
    }
}
