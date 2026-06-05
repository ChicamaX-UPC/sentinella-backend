package com.chicamax.sentinella.bootstrap;

import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertCommand;
import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertRuleCommand;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertRuleOperator;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.services.AlertCommandService;
import com.chicamax.sentinella.alerts.domain.services.AlertRuleCommandService;
import com.chicamax.sentinella.fieldoperations.domain.model.commands.CreateRoundCommand;
import com.chicamax.sentinella.fieldoperations.domain.services.InspectionRoundCommandService;
import com.chicamax.sentinella.iam.domain.model.aggregates.User;
import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import com.chicamax.sentinella.iam.domain.services.HashingService;
import com.chicamax.sentinella.iam.infrastructure.persistence.jpa.UserRepository;
import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.domain.model.commands.RegisterSensorReadingCommand;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorStatus;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import com.chicamax.sentinella.monitoring.domain.services.SensorReadingCommandService;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import com.chicamax.sentinella.simulations.domain.model.commands.CreateSimulationScenarioCommand;
import com.chicamax.sentinella.simulations.domain.model.valueobjects.SimulationType;
import com.chicamax.sentinella.simulations.domain.services.SimulationScenarioCommandService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Datos demo coherentes (tranque Río Chicama, sensores, alerta, ronda, simulación).
 * Idempotente: si existe admin@sentinella.demo no vuelve a insertar.
 */
@Component
@Order(50)
@ConditionalOnProperty(name = "sentinella.seed.enabled", havingValue = "true")
public class SentinellaDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SentinellaDataSeeder.class);

    /** Tranque de relaves ficticio pero estable para JWT y FK. */
    public static final UUID DAM_CHICAMA_NORTE = UUID.fromString("a1e8c0de-4b2a-4c1f-9f3d-8c7b6a5d4e3f");

    public static final String DEMO_PASSWORD = "Sentinella2024!";

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final SensorNodeRepository sensorNodeRepository;
    private final SensorReadingCommandService sensorReadingCommandService;
    private final AlertRuleCommandService alertRuleCommandService;
    private final AlertCommandService alertCommandService;
    private final InspectionRoundCommandService inspectionRoundCommandService;
    private final SimulationScenarioCommandService simulationScenarioCommandService;
    private final TransactionTemplate transactionTemplate;

    public SentinellaDataSeeder(
            UserRepository userRepository,
            HashingService hashingService,
            SensorNodeRepository sensorNodeRepository,
            SensorReadingCommandService sensorReadingCommandService,
            AlertRuleCommandService alertRuleCommandService,
            AlertCommandService alertCommandService,
            InspectionRoundCommandService inspectionRoundCommandService,
            SimulationScenarioCommandService simulationScenarioCommandService,
            PlatformTransactionManager transactionManager
    ) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.sensorNodeRepository = sensorNodeRepository;
        this.sensorReadingCommandService = sensorReadingCommandService;
        this.alertRuleCommandService = alertRuleCommandService;
        this.alertCommandService = alertCommandService;
        this.inspectionRoundCommandService = inspectionRoundCommandService;
        this.simulationScenarioCommandService = simulationScenarioCommandService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        transactionTemplate.executeWithoutResult(status -> seedTransactional());
    }

    private void seedTransactional() {
        if (userRepository.existsByEmail("admin@sentinella.demo")) {
            log.info("sentinella.seed: datos demo ya presentes (admin@sentinella.demo), se omite.");
            return;
        }

        UUID[] damScope = new UUID[]{DAM_CHICAMA_NORTE};
        String hash = hashingService.hash(DEMO_PASSWORD);

        User admin = userRepository.save(new User(
                UUID.fromString("b0000001-0001-4001-8001-000000000001"),
                "admin@sentinella.demo",
                hash,
                "María Quispe — Administración TI",
                Role.SYSTEM_ADMIN,
                damScope
        ));
        User manager = userRepository.save(new User(
                UUID.fromString("b0000002-0002-4002-8002-000000000002"),
                "jefe.planta@sentinella.demo",
                hash,
                "Carlos Ríos — Jefe de planta relaves",
                Role.PLANT_MANAGER,
                damScope
        ));
        User operator = userRepository.save(new User(
                UUID.fromString("b0000003-0003-4003-8003-000000000003"),
                "campo@sentinella.demo",
                hash,
                "Luis Huamán — Operario de relavera",
                Role.FIELD_OPERATOR,
                damScope
        ));
        userRepository.save(new User(
                UUID.fromString("b0000004-0004-4004-8004-000000000004"),
                "auditoria@sentinella.demo",
                hash,
                "Ana Márquez — Solo lectura OEFA",
                Role.READ_ONLY,
                damScope
        ));

        UUID nodeNivel = UUID.fromString("c1000001-0001-4001-8001-000000000001");
        UUID nodePiezo = UUID.fromString("c1000002-0002-4002-8002-000000000002");
        UUID nodePluvio = UUID.fromString("c1000003-0003-4003-8003-000000000003");

        OffsetDateTime now = OffsetDateTime.now();

        sensorNodeRepository.save(new SensorNode(
                nodeNivel,
                "NW-REL-01",
                "Nivel de relave — cota corona (barcaza)",
                DAM_CHICAMA_NORTE,
                SensorType.WATER_LEVEL,
                new BigDecimal("-7.9234000"),
                new BigDecimal("-78.5123000"),
                "{\"x\":0,\"y\":10,\"z\":0}",
                "ONLINE",
                now
        ));
        sensorNodeRepository.save(new SensorNode(
                nodePiezo,
                "PI-REL-02",
                "Piezómetro — talud aguas abajo SE",
                DAM_CHICAMA_NORTE,
                SensorType.PRESSURE,
                new BigDecimal("-7.9241000"),
                new BigDecimal("-78.5110000"),
                "{\"x\":-35,\"y\":2,\"z\":45}",
                "ONLINE",
                now
        ));
        sensorNodeRepository.save(new SensorNode(
                nodePluvio,
                "PV-REL-01",
                "Pluviómetro — estación perimetral NW",
                DAM_CHICAMA_NORTE,
                SensorType.PLUVIOMETER,
                new BigDecimal("-7.9180000"),
                new BigDecimal("-78.5200000"),
                null,
                "ONLINE",
                now.minusHours(1)
        ));

        var ruleNivel = alertRuleCommandService.create(new CreateAlertRuleCommand(
                nodeNivel,
                SensorType.WATER_LEVEL.name(),
                AlertRuleOperator.GT,
                new BigDecimal("784.2000"),
                AlertSeverity.WARNING,
                new AlertChannel[]{AlertChannel.APP, AlertChannel.EMAIL},
                30,
                admin.getId()
        ));

        alertCommandService.create(new CreateAlertCommand(
                ruleNivel.getId(),
                nodeNivel,
                SensorType.WATER_LEVEL.name(),
                new BigDecimal("784.6500"),
                AlertSeverity.WARNING,
                admin.getId(),
                Role.SYSTEM_ADMIN.name()
        ));

        sensorReadingCommandService.handle(new RegisterSensorReadingCommand(
                nodeNivel,
                now,
                SensorType.WATER_LEVEL,
                new BigDecimal("784.55"),
                "msnm",
                SensorStatus.WARNING,
                "{\"source\":\"seed\"}"
        ));
        sensorReadingCommandService.handle(new RegisterSensorReadingCommand(
                nodePiezo,
                now.minusMinutes(15),
                SensorType.PRESSURE,
                new BigDecimal("125.4"),
                "kPa",
                SensorStatus.OK,
                "{\"source\":\"seed\"}"
        ));
        sensorReadingCommandService.handle(new RegisterSensorReadingCommand(
                nodePluvio,
                now.minusHours(6),
                SensorType.PLUVIOMETER,
                new BigDecimal("12.5"),
                "mm/h",
                SensorStatus.OK,
                "{\"source\":\"seed\"}"
        ));

        inspectionRoundCommandService.createRound(new CreateRoundCommand(
                operator.getId(),
                DAM_CHICAMA_NORTE,
                now.plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0),
                false
        ));

        simulationScenarioCommandService.create(new CreateSimulationScenarioCommand(
                "Lluvia extrema — escenario Q100",
                "Precipitación sostenida 48 h sobre cuenca aportante; revisión canal de coronación.",
                SimulationType.HEAVY_RAIN,
                "{\"rainMmPerHour\":42,\"durationHours\":18,\"returnPeriod\":\"Q100\"}",
                DAM_CHICAMA_NORTE,
                manager.getId()
        ));

        log.info(
                "sentinella.seed: usuarios demo creados (contraseña '{}'). Admin={}",
                DEMO_PASSWORD,
                admin.getEmail()
        );
    }
}
