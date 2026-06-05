package com.chicamax.sentinella.simulations.application.internal.queryservices;

import com.chicamax.sentinella.simulations.domain.model.aggregates.SimulationScenario;
import com.chicamax.sentinella.simulations.domain.services.SimulationScenarioQueryService;
import com.chicamax.sentinella.simulations.infrastructure.persistence.jpa.SimulationScenarioRepository;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SimulationScenarioQueryServiceImpl implements SimulationScenarioQueryService {

    private final SimulationScenarioRepository repository;
    private final AuthorizationScopeService authorizationScopeService;

    public SimulationScenarioQueryServiceImpl(
            SimulationScenarioRepository repository,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.repository = repository;
        this.authorizationScopeService = authorizationScopeService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimulationScenario> listForJwt(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        UUID userId = UUID.fromString(jwt.getSubject());
        Set<UUID> damIds = authorizationScopeService.extractDamIds(jwt);

        if ("SYSTEM_ADMIN".equals(role)) {
            return repository.findAll().stream()
                    .sorted(Comparator.comparing(SimulationScenario::getUpdatedAt).reversed())
                    .toList();
        }
        if ("READ_ONLY".equals(role)) {
            if (damIds.isEmpty()) {
                return repository.findByIsPublicTrue().stream()
                        .sorted(Comparator.comparing(SimulationScenario::getUpdatedAt).reversed())
                        .toList();
            }
            return repository.findByIsPublicTrueAndTailingDamIdIn(damIds).stream()
                    .sorted(Comparator.comparing(SimulationScenario::getUpdatedAt).reversed())
                    .toList();
        }
        // PLANT_MANAGER (u otros roles autorizados en el controlador)
        if (damIds.isEmpty()) {
            return mergeDistinct(
                    repository.findByCreatedBy(userId),
                    repository.findByIsPublicTrue()
            );
        }
        List<SimulationScenario> own = repository.findByTailingDamIdInAndCreatedBy(damIds, userId);
        List<SimulationScenario> pub = repository.findByTailingDamIdInAndIsPublicIsTrue(damIds);
        return mergeDistinct(own, pub);
    }

    private static List<SimulationScenario> mergeDistinct(List<SimulationScenario> a, List<SimulationScenario> b) {
        return Stream.concat(a.stream(), b.stream())
                .collect(java.util.stream.Collectors.toMap(
                        SimulationScenario::getId,
                        s -> s,
                        (x, y) -> x,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(SimulationScenario::getUpdatedAt).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SimulationScenario> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public boolean canRead(Jwt jwt, SimulationScenario scenario) {
        String role = jwt.getClaimAsString("role");
        UUID userId = UUID.fromString(jwt.getSubject());
        if ("SYSTEM_ADMIN".equals(role)) {
            return true;
        }
        if (!authorizationScopeService.canAccessDam(jwt, scenario.getTailingDamId())) {
            return false;
        }
        if ("READ_ONLY".equals(role)) {
            return scenario.isPublic();
        }
        if ("PLANT_MANAGER".equals(role)) {
            return scenario.isPublic() || scenario.getCreatedBy().equals(userId);
        }
        return false;
    }
}
