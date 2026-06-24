package com.chicamax.sentinella.monitoring.interfaces.rest;

import com.chicamax.sentinella.monitoring.application.internal.prediction.PredictiveRiskQueryService;
import com.chicamax.sentinella.monitoring.application.internal.prediction.TelemetryPatternAnalysisService;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.PredictiveRiskResource;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.TelemetryPatternsResource;
import com.chicamax.sentinella.monitoring.domain.services.SensorNodeQueryService;
import com.chicamax.sentinella.shared.infrastructure.security.AuthorizationScopeService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/analytics")
public class AnalyticsController {

    private final TelemetryPatternAnalysisService telemetryPatternAnalysisService;
    private final PredictiveRiskQueryService predictiveRiskQueryService;
    private final SensorNodeQueryService sensorNodeQueryService;
    private final AuthorizationScopeService authorizationScopeService;

    public AnalyticsController(
            TelemetryPatternAnalysisService telemetryPatternAnalysisService,
            PredictiveRiskQueryService predictiveRiskQueryService,
            SensorNodeQueryService sensorNodeQueryService,
            AuthorizationScopeService authorizationScopeService
    ) {
        this.telemetryPatternAnalysisService = telemetryPatternAnalysisService;
        this.predictiveRiskQueryService = predictiveRiskQueryService;
        this.sensorNodeQueryService = sensorNodeQueryService;
        this.authorizationScopeService = authorizationScopeService;
    }

    @GetMapping("/patterns")
    public ResponseEntity<TelemetryPatternsResource> getPatterns(@AuthenticationPrincipal Jwt jwt) {
        Set<UUID> damIds = authorizationScopeService.extractDamIds(jwt);
        var result = telemetryPatternAnalysisService.analyze(damIds);
        return ResponseEntity.ok(new TelemetryPatternsResource(
                result.waterRainCorrelation(),
                result.avgRainMmPerHour(),
                result.rateOfChangeByType().stream()
                        .map(r -> new TelemetryPatternsResource.RateOfChangeResource(
                                r.sensorType(), r.avgAbsDelta(), r.maxAbsDelta()))
                        .toList(),
                result.documentedPatterns()
        ));
    }

    @GetMapping("/predictive-risks")
    public ResponseEntity<List<PredictiveRiskResource>> getPredictiveRisks(@AuthenticationPrincipal Jwt jwt) {
        Set<UUID> damIds = authorizationScopeService.extractDamIds(jwt);
        List<PredictiveRiskResource> risks = predictiveRiskQueryService.listRisksWithinWindow().stream()
                .filter(r -> canAccessNodeDam(r.nodeId(), damIds, jwt))
                .map(r -> new PredictiveRiskResource(
                        r.ruleId(),
                        r.nodeId(),
                        r.nodeName(),
                        r.sensorType(),
                        r.thresholdValue(),
                        r.currentValue(),
                        r.slopePerHour(),
                        r.estimatedBreachAt(),
                        r.leadTimeMinutes(),
                        r.severity()
                ))
                .toList();
        return ResponseEntity.ok(risks);
    }

    private boolean canAccessNodeDam(UUID nodeId, Set<UUID> damIds, Jwt jwt) {
        return sensorNodeQueryService.findById(nodeId)
                .map(node -> authorizationScopeService.canAccessDam(jwt, node.getTailingDamId()))
                .orElse(false);
    }
}
