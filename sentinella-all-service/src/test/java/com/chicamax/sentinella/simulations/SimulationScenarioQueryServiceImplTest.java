package com.chicamax.sentinella.simulations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import com.chicamax.sentinella.simulations.application.internal.queryservices.SimulationScenarioQueryServiceImpl;
import com.chicamax.sentinella.simulations.domain.model.aggregates.SimulationScenario;
import com.chicamax.sentinella.simulations.domain.model.valueobjects.SimulationType;
import com.chicamax.sentinella.simulations.infrastructure.persistence.jpa.SimulationScenarioRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class SimulationScenarioQueryServiceImplTest {

    @Mock
    private SimulationScenarioRepository repository;

    @Mock
    private AuthorizationScopeService authorizationScopeService;

    @InjectMocks
    private SimulationScenarioQueryServiceImpl queryService;

    @Test
    void readOnlyCanReadPublicScenarioInScope() {
        UUID dam = UUID.randomUUID();
        UUID user = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("x")
                .headers(h -> h.put("alg", "none"))
                .claim("sub", user.toString())
                .claim("role", "READ_ONLY")
                .claim("damIds", List.of(dam.toString()))
                .build();
        SimulationScenario scenario = new SimulationScenario(
                UUID.randomUUID(),
                "n",
                null,
                SimulationType.HEAVY_RAIN,
                "{}",
                dam,
                UUID.randomUUID()
        );
        scenario.publish();
        when(authorizationScopeService.canAccessDam(jwt, dam)).thenReturn(true);
        assertTrue(queryService.canRead(jwt, scenario));
    }

    @Test
    void readOnlyCannotReadPrivateScenario() {
        UUID dam = UUID.randomUUID();
        UUID user = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("x")
                .headers(h -> h.put("alg", "none"))
                .claim("sub", user.toString())
                .claim("role", "READ_ONLY")
                .claim("damIds", List.of(dam.toString()))
                .build();
        SimulationScenario scenario = new SimulationScenario(
                UUID.randomUUID(),
                "n",
                null,
                SimulationType.HEAVY_RAIN,
                "{}",
                dam,
                UUID.randomUUID()
        );
        when(authorizationScopeService.canAccessDam(jwt, dam)).thenReturn(true);
        assertFalse(queryService.canRead(jwt, scenario));
    }

    @Test
    void plantManagerCanReadOwnPrivateScenario() {
        UUID dam = UUID.randomUUID();
        UUID user = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("x")
                .headers(h -> h.put("alg", "none"))
                .claim("sub", user.toString())
                .claim("role", "PLANT_MANAGER")
                .claim("damIds", List.of(dam.toString()))
                .build();
        SimulationScenario scenario = new SimulationScenario(
                UUID.randomUUID(),
                "n",
                null,
                SimulationType.HEAVY_RAIN,
                "{}",
                dam,
                user
        );
        when(authorizationScopeService.canAccessDam(jwt, dam)).thenReturn(true);
        assertTrue(queryService.canRead(jwt, scenario));
    }
}
