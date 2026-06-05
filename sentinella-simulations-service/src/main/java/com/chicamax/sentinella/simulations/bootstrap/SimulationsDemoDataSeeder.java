package com.chicamax.sentinella.simulations.bootstrap;

import com.chicamax.sentinella.simulations.domain.model.commands.CreateSimulationScenarioCommand;
import com.chicamax.sentinella.simulations.domain.model.valueobjects.SimulationType;
import com.chicamax.sentinella.simulations.domain.services.SimulationScenarioCommandService;
import com.chicamax.sentinella.simulations.infrastructure.persistence.jpa.SimulationScenarioRepository;
import com.chicamax.sentinella.shared.bootstrap.demo.SentinellaDemoIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@Order(100)
@ConditionalOnProperty(name = "sentinella.seed.enabled", havingValue = "true")
public class SimulationsDemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SimulationsDemoDataSeeder.class);

    private final SimulationScenarioRepository simulationScenarioRepository;
    private final SimulationScenarioCommandService simulationScenarioCommandService;
    private final TransactionTemplate transactionTemplate;

    public SimulationsDemoDataSeeder(
            SimulationScenarioRepository simulationScenarioRepository,
            SimulationScenarioCommandService simulationScenarioCommandService,
            PlatformTransactionManager transactionManager
    ) {
        this.simulationScenarioRepository = simulationScenarioRepository;
        this.simulationScenarioCommandService = simulationScenarioCommandService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        transactionTemplate.executeWithoutResult(status -> seed());
    }

    private void seed() {
        if (simulationScenarioRepository.count() > 0) {
            log.info("sentinella.seed (simulations): escenarios demo ya presentes, se omite.");
            return;
        }

        simulationScenarioCommandService.create(new CreateSimulationScenarioCommand(
                "Lluvia extrema — escenario Q100",
                "Precipitación sostenida 48 h sobre cuenca aportante; revisión canal de coronación.",
                SimulationType.HEAVY_RAIN,
                "{\"rainMmPerHour\":42,\"durationHours\":18,\"returnPeriod\":\"Q100\"}",
                SentinellaDemoIds.TAILING_DAM_CHICAMA_NORTE,
                SentinellaDemoIds.USER_MANAGER
        ));

        log.info("sentinella.seed (simulations): escenario demo creado.");
    }
}
