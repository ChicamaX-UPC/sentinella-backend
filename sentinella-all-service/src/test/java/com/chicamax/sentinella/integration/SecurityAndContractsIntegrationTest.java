package com.chicamax.sentinella.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chicamax.sentinella.alerts.domain.model.entities.AlertRule;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertChannel;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertRuleOperator;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.services.AlertRuleCommandService;
import com.chicamax.sentinella.dashboard.domain.services.DashboardQueryService;
import com.chicamax.sentinella.dashboard.interfaces.rest.resources.ExecutiveDashboardResource;
import com.chicamax.sentinella.fieldoperations.domain.model.aggregates.InspectionRound;
import com.chicamax.sentinella.fieldoperations.domain.services.InspectionRoundCommandService;
import com.chicamax.sentinella.fieldoperations.domain.services.InspectionRoundQueryService;
import com.chicamax.sentinella.iam.domain.model.aggregates.User;
import com.chicamax.sentinella.iam.domain.model.queries.GetAllUsersQuery;
import com.chicamax.sentinella.iam.domain.model.queries.GetUserByEmailQuery;
import com.chicamax.sentinella.iam.domain.model.valueobjects.AuthTokens;
import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import com.chicamax.sentinella.iam.domain.services.UserCommandService;
import com.chicamax.sentinella.iam.domain.services.UserQueryService;
import com.chicamax.sentinella.nodeadmin.domain.model.aggregates.IoTNode;
import com.chicamax.sentinella.nodeadmin.domain.services.IoTNodeCommandService;
import com.chicamax.sentinella.nodeadmin.domain.services.IoTNodeQueryService;
import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.domain.services.SensorNodeQueryService;
import com.chicamax.sentinella.reports.domain.model.aggregates.Report;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportFormat;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportType;
import com.chicamax.sentinella.reports.domain.services.ReportCommandService;
import com.chicamax.sentinella.reports.domain.services.ReportQueryService;
import com.chicamax.sentinella.simulations.domain.model.aggregates.SimulationScenario;
import com.chicamax.sentinella.simulations.domain.model.valueobjects.SimulationType;
import com.chicamax.sentinella.simulations.domain.services.SimulationScenarioCommandService;
import com.chicamax.sentinella.simulations.domain.services.SimulationScenarioQueryService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAndContractsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCommandService userCommandService;

    @MockitoBean
    private UserQueryService userQueryService;

    @MockitoBean
    private AlertRuleCommandService alertRuleCommandService;

    @MockitoBean
    private IoTNodeCommandService ioTNodeCommandService;

    @MockitoBean
    private IoTNodeQueryService ioTNodeQueryService;

    @MockitoBean
    private InspectionRoundQueryService inspectionRoundQueryService;

    @MockitoBean
    private InspectionRoundCommandService inspectionRoundCommandService;

    @MockitoBean
    private ReportCommandService reportCommandService;

    @MockitoBean
    private ReportQueryService reportQueryService;

    @MockitoBean
    private DashboardQueryService dashboardQueryService;

    @MockitoBean
    private SensorNodeQueryService sensorNodeQueryService;

    @MockitoBean
    private SimulationScenarioCommandService simulationScenarioCommandService;

    @MockitoBean
    private SimulationScenarioQueryService simulationScenarioQueryService;

    @BeforeEach
    void stubRoundChecklist() {
        Mockito.lenient()
                .when(inspectionRoundQueryService.findChecklistItemsByRoundId(Mockito.any()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    void loginShouldBePublicAndReturnContract() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User(
                userId,
                "manager@sentinella.io",
                "hash",
                "Manager",
                Role.PLANT_MANAGER,
                new UUID[0]
        );
        when(userCommandService.signIn(any())).thenReturn(new AuthTokens("token-1", "refresh-1", 900));
        when(userQueryService.handle(Mockito.any(GetUserByEmailQuery.class))).thenReturn(Optional.of(user));

        mockMvc.perform(post("/v1/auth/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "manager@sentinella.io",
                                  "password": "secret123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-1"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-1"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.email").value("manager@sentinella.io"));
    }

    @Test
    void usersEndpointShouldRejectNonAdmin() throws Exception {
        mockMvc.perform(get("/v1/users")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "FIELD_OPERATOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_FIELD_OPERATOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void usersEndpointShouldAllowAdmin() throws Exception {
        when(userQueryService.handle(Mockito.any(GetAllUsersQuery.class))).thenReturn(List.of());

        mockMvc.perform(get("/v1/users")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "SYSTEM_ADMIN"))
                                .authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void alertRulesShouldAllowPlantManagerCreate() throws Exception {
        AlertRule created = new AlertRule(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "water_level",
                AlertRuleOperator.GT,
                BigDecimal.valueOf(10),
                AlertSeverity.WARNING,
                new AlertChannel[]{AlertChannel.APP},
                30,
                UUID.randomUUID()
        );
        when(alertRuleCommandService.create(any())).thenReturn(created);

        mockMvc.perform(post("/v1/alert-rules")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "PLANT_MANAGER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PLANT_MANAGER")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "nodeId": "%s",
                                  "sensorType": "water_level",
                                  "operator": "GT",
                                  "thresholdValue": 10.0,
                                  "severity": "WARNING",
                                  "channels": ["APP"],
                                  "escalationMinutes": 30
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensorType").value("water_level"))
                .andExpect(jsonPath("$.operator").value("GT"));
    }

    @Test
    void nodeRegistrationShouldRejectPlantManager() throws Exception {
        mockMvc.perform(post("/v1/nodes")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "PLANT_MANAGER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PLANT_MANAGER")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "externalId": "N-100",
                                  "name": "Nodo Test",
                                  "tailingDamId": "%s",
                                  "sensorType": "water_level"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void nodeRegistrationShouldAllowAdmin() throws Exception {
        IoTNode node = new IoTNode(
                UUID.randomUUID(),
                "N-101",
                "Nodo Admin",
                UUID.randomUUID(),
                "water_level",
                null,
                null,
                null
        );
        when(ioTNodeCommandService.register(any())).thenReturn(node);

        mockMvc.perform(post("/v1/nodes")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "SYSTEM_ADMIN"))
                                .authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "externalId": "N-101",
                                  "name": "Nodo Admin",
                                  "tailingDamId": "%s",
                                  "sensorType": "water_level"
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value("N-101"));
    }

    @Test
    void roundsShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/v1/rounds"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dashboardShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/v1/dashboard/executive"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void alertPatchShouldRejectCloseForFieldOperator() throws Exception {
        mockMvc.perform(patch("/v1/alerts/{alertId}", UUID.randomUUID())
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "FIELD_OPERATOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_FIELD_OPERATOR")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "action": "CLOSE",
                                  "notes": "cerrando"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void reportGenerateShouldRejectReadOnly() throws Exception {
        mockMvc.perform(post("/v1/reports/generate")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "READ_ONLY"))
                                .authorities(new SimpleGrantedAuthority("ROLE_READ_ONLY")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "type": "ALERT_HISTORY",
                                  "from": "2026-04-01T00:00:00Z",
                                  "to": "2026-04-22T00:00:00Z",
                                  "format": "PDF"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void reportGenerateShouldAllowPlantManager() throws Exception {
        Report report = new Report(
                UUID.randomUUID(),
                ReportType.ALERT_HISTORY,
                ReportFormat.PDF,
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-01T00:00:00Z"),
                OffsetDateTime.parse("2026-04-22T00:00:00Z"),
                UUID.randomUUID(),
                "build/reports/test.pdf"
        );
        when(reportCommandService.generate(any())).thenReturn(report);

        mockMvc.perform(post("/v1/reports/generate")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "PLANT_MANAGER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PLANT_MANAGER")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "type": "ALERT_HISTORY",
                                  "from": "2026-04-01T00:00:00Z",
                                  "to": "2026-04-22T00:00:00Z",
                                  "format": "PDF"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ALERT_HISTORY"))
                .andExpect(jsonPath("$.format").value("PDF"));
    }

    @Test
    void dashboardExecutiveShouldAllowReadOnly() throws Exception {
        when(dashboardQueryService.getExecutive(any())).thenReturn(new ExecutiveDashboardResource(10, 3, 1, 8));

        mockMvc.perform(get("/v1/dashboard/executive")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "READ_ONLY"))
                                .authorities(new SimpleGrantedAuthority("ROLE_READ_ONLY"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNodes").value(10));
    }

    @Test
    void dashboardExecutiveShouldRejectFieldOperator() throws Exception {
        mockMvc.perform(get("/v1/dashboard/executive")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "FIELD_OPERATOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_FIELD_OPERATOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void roundsCreateShouldAllowFieldOperator() throws Exception {
        InspectionRound round = new InspectionRound(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                OffsetDateTime.parse("2026-04-22T08:00:00Z"),
                true
        );
        when(inspectionRoundCommandService.createRound(any())).thenReturn(round);

        mockMvc.perform(post("/v1/rounds")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "FIELD_OPERATOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_FIELD_OPERATOR")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "tailingDamId": "%s",
                                  "scheduledAt": "2026-04-22T08:00:00Z",
                                  "offlineCreated": true
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offlineCreated").value(true));
    }

    @Test
    void reportGenerateShouldRejectWhenDamIsOutOfScope() throws Exception {
        UUID allowedDam = UUID.randomUUID();
        UUID requestedDam = UUID.randomUUID();

        mockMvc.perform(post("/v1/reports/generate")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "PLANT_MANAGER")
                                .claim("damIds", List.of(allowedDam.toString())))
                                .authorities(new SimpleGrantedAuthority("ROLE_PLANT_MANAGER")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "type": "ALERT_HISTORY",
                                  "from": "2026-04-01T00:00:00Z",
                                  "to": "2026-04-22T00:00:00Z",
                                  "format": "PDF",
                                  "tailingDamId": "%s"
                                }
                                """.formatted(requestedDam)))
                .andExpect(status().isForbidden());
    }

    @Test
    void nodesListShouldFilterByDamIdsClaim() throws Exception {
        SensorNode allowed = org.mockito.Mockito.mock(SensorNode.class);
        SensorNode denied = org.mockito.Mockito.mock(SensorNode.class);
        UUID allowedDam = UUID.randomUUID();
        UUID deniedDam = UUID.randomUUID();
        when(allowed.getId()).thenReturn(UUID.randomUUID());
        when(allowed.getExternalId()).thenReturn("N-ALLOWED");
        when(allowed.getName()).thenReturn("Nodo Permitido");
        when(allowed.getTailingDamId()).thenReturn(allowedDam);
        when(allowed.getSensorType()).thenReturn(com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType.WATER_LEVEL);
        when(allowed.getStatus()).thenReturn("ACTIVE");

        when(denied.getId()).thenReturn(UUID.randomUUID());
        when(denied.getExternalId()).thenReturn("N-DENIED");
        when(denied.getName()).thenReturn("Nodo Denegado");
        when(denied.getTailingDamId()).thenReturn(deniedDam);
        when(denied.getSensorType()).thenReturn(com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType.WATER_LEVEL);
        when(denied.getStatus()).thenReturn("ACTIVE");

        when(sensorNodeQueryService.handle(any())).thenReturn(List.of(allowed, denied));

        mockMvc.perform(get("/v1/nodes")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "PLANT_MANAGER")
                                .claim("damIds", List.of(allowedDam.toString())))
                                .authorities(new SimpleGrantedAuthority("ROLE_PLANT_MANAGER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].externalId").value("N-ALLOWED"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void simulationScenariosListShouldRejectFieldOperator() throws Exception {
        mockMvc.perform(get("/v1/simulation-scenarios")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "FIELD_OPERATOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_FIELD_OPERATOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void simulationScenariosListShouldAllowReadOnly() throws Exception {
        SimulationScenario scenario = Mockito.mock(SimulationScenario.class);
        when(scenario.getId()).thenReturn(UUID.randomUUID());
        when(scenario.getName()).thenReturn("Public scenario");
        when(scenario.getDescription()).thenReturn(null);
        when(scenario.getSimulationType()).thenReturn(SimulationType.HEAVY_RAIN);
        when(scenario.getParameters()).thenReturn("{\"rain_intensity\":45}");
        when(scenario.getTailingDamId()).thenReturn(UUID.randomUUID());
        when(scenario.getCreatedBy()).thenReturn(UUID.randomUUID());
        when(scenario.isPublic()).thenReturn(true);
        when(scenario.getCreatedAt()).thenReturn(OffsetDateTime.parse("2026-04-01T00:00:00Z"));
        when(scenario.getUpdatedAt()).thenReturn(OffsetDateTime.parse("2026-04-02T00:00:00Z"));
        when(simulationScenarioQueryService.listForJwt(any())).thenReturn(List.of(scenario));

        mockMvc.perform(get("/v1/simulation-scenarios")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "READ_ONLY"))
                                .authorities(new SimpleGrantedAuthority("ROLE_READ_ONLY"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Public scenario"))
                .andExpect(jsonPath("$[0].simulationType").value("HEAVY_RAIN"));
    }

    @Test
    void simulationScenariosCreateShouldAllowPlantManager() throws Exception {
        UUID damId = UUID.randomUUID();
        SimulationScenario created = Mockito.mock(SimulationScenario.class);
        when(created.getId()).thenReturn(UUID.randomUUID());
        when(created.getName()).thenReturn("Lluvia extrema");
        when(created.getDescription()).thenReturn("Test");
        when(created.getSimulationType()).thenReturn(SimulationType.HEAVY_RAIN);
        when(created.getParameters()).thenReturn("{\"rain_intensity\":50}");
        when(created.getTailingDamId()).thenReturn(damId);
        when(created.getCreatedBy()).thenReturn(UUID.randomUUID());
        when(created.isPublic()).thenReturn(false);
        when(created.getCreatedAt()).thenReturn(OffsetDateTime.parse("2026-04-01T00:00:00Z"));
        when(created.getUpdatedAt()).thenReturn(OffsetDateTime.parse("2026-04-01T00:00:00Z"));
        when(simulationScenarioCommandService.create(any())).thenReturn(created);

        mockMvc.perform(post("/v1/simulation-scenarios")
                        .with(jwt().jwt(jwt -> jwt
                                .subject(UUID.randomUUID().toString())
                                .claim("role", "PLANT_MANAGER")
                                .claim("damIds", List.of(damId.toString())))
                                .authorities(new SimpleGrantedAuthority("ROLE_PLANT_MANAGER")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Lluvia extrema",
                                  "description": "Test",
                                  "simulationType": "HEAVY_RAIN",
                                  "parameters": {"rain_intensity": 50},
                                  "tailingDamId": "%s"
                                }
                                """.formatted(damId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lluvia extrema"))
                .andExpect(jsonPath("$.simulationType").value("HEAVY_RAIN"));
    }
}
