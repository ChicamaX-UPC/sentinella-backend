package com.chicamax.sentinella.simulations.domain.services;

import com.chicamax.sentinella.simulations.domain.model.aggregates.SimulationScenario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

public interface SimulationScenarioQueryService {

    List<SimulationScenario> listForJwt(Jwt jwt);

    Optional<SimulationScenario> findById(UUID id);

    boolean canRead(Jwt jwt, SimulationScenario scenario);
}
