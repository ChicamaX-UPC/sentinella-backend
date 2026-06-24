package com.chicamax.sentinella.dashboard.application.internal.queryservices;

import com.chicamax.sentinella.dashboard.application.internal.cache.ExecutiveKpiCache;
import com.chicamax.sentinella.dashboard.config.DashboardDownstreamProperties;
import com.chicamax.sentinella.dashboard.domain.model.queries.GetExecutiveDashboardQuery;
import com.chicamax.sentinella.dashboard.domain.model.queries.GetFieldDashboardQuery;
import com.chicamax.sentinella.dashboard.domain.model.queries.GetNodesMapQuery;
import com.chicamax.sentinella.dashboard.domain.services.DashboardQueryService;
import com.chicamax.sentinella.dashboard.interfaces.rest.resources.ExecutiveDashboardResource;
import com.chicamax.sentinella.dashboard.interfaces.rest.resources.FieldDashboardResource;
import com.chicamax.sentinella.dashboard.interfaces.rest.resources.NodesMapResource;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class DashboardQueryServiceImpl implements DashboardQueryService {

    private final RestClient monitoring;
    private final RestClient alerts;
    private final RestClient fieldops;
    private final int recentDataHours;
    private final ExecutiveKpiCache executiveKpiCache;

    public DashboardQueryServiceImpl(
            DashboardDownstreamProperties props,
            @Value("${sentinella.dashboard.recent-data-hours:1}") int recentDataHours,
            ExecutiveKpiCache executiveKpiCache
    ) {
        this.monitoring = RestClient.builder().baseUrl(props.monitoringBaseUrl()).build();
        this.alerts = RestClient.builder().baseUrl(props.alertsBaseUrl()).build();
        this.fieldops = RestClient.builder().baseUrl(props.fieldopsBaseUrl()).build();
        this.recentDataHours = Math.max(1, recentDataHours);
        this.executiveKpiCache = executiveKpiCache;
    }

    @Override
    public ExecutiveDashboardResource getExecutive(GetExecutiveDashboardQuery query, Jwt jwt) {
        Set<UUID> damIds = normalizeDamIds(query.damIds());
        return executiveKpiCache.get(damIds).orElseGet(() -> {
            ExecutiveDashboardResource computed = computeExecutive(damIds, jwt);
            executiveKpiCache.put(damIds, computed);
            return computed;
        });
    }

    private ExecutiveDashboardResource computeExecutive(Set<UUID> damIds, Jwt jwt) {
        Map<UUID, WireNode> nodesById = loadNodesScoped(jwt, damIds);
        List<WireAlert> alertList = loadAlerts(jwt, damIds);
        OffsetDateTime from = OffsetDateTime.now().minusHours(recentDataHours);

        long totalNodes = nodesById.size();
        long activeAlerts = alertList.stream()
                .filter(alert -> nodeBelongsToDamScope(alert.nodeId(), damIds, nodesById))
                .filter(alert -> "RECEIVED".equals(alert.status()) || "ACTIVE".equals(alert.status()))
                .count();
        long criticalAlerts = alertList.stream()
                .filter(alert -> nodeBelongsToDamScope(alert.nodeId(), damIds, nodesById))
                .filter(alert -> "CRITICAL".equals(alert.severity()))
                .count();

        long nodesWithRecentData = countNodesWithRecentData(jwt, nodesById, from);

        long predictiveRiskNodes = loadPredictiveRisks(jwt).stream()
                .filter(risk -> nodeBelongsToDamScope(risk.nodeId(), damIds, nodesById))
                .count();

        return new ExecutiveDashboardResource(totalNodes, activeAlerts, criticalAlerts, nodesWithRecentData, predictiveRiskNodes);
    }

    @Override
    public FieldDashboardResource getField(GetFieldDashboardQuery query, Jwt jwt) {
        Set<UUID> damIds = normalizeDamIds(query.damIds());
        Map<UUID, WireNode> nodesById = loadNodesScoped(jwt, damIds);
        List<WireAlert> alertList = loadAlerts(jwt, damIds);
        List<WireRound> rounds = loadRounds(jwt);

        long activeAlerts = alertList.stream()
                .filter(alert -> nodeBelongsToDamScope(alert.nodeId(), damIds, nodesById))
                .filter(alert -> "RECEIVED".equals(alert.status()) || "ACTIVE".equals(alert.status()))
                .count();
        long roundsInProgress = rounds.stream()
                .filter(round -> damIds.isEmpty() || damIds.contains(round.tailingDamId()))
                .filter(round -> "IN_PROGRESS".equals(round.status()))
                .count();
        long pendingSync = rounds.stream()
                .filter(round -> damIds.isEmpty() || damIds.contains(round.tailingDamId()))
                .filter(round -> "COMPLETED".equals(round.status()))
                .count();
        long sensorsOutOfRange = nodesById.values().stream()
                .filter(node -> {
                    String st = node.status() == null ? "" : node.status().toUpperCase();
                    return st.contains("WARN") || st.contains("CRIT") || st.contains("OFFLINE");
                })
                .count();
        OffsetDateTime lastIncidentAt = alertList.stream()
                .filter(alert -> nodeBelongsToDamScope(alert.nodeId(), damIds, nodesById))
                .map(WireAlert::createdAt)
                .filter(ts -> ts != null)
                .max(OffsetDateTime::compareTo)
                .orElse(null);
        return new FieldDashboardResource(
                activeAlerts,
                roundsInProgress,
                pendingSync,
                sensorsOutOfRange,
                lastIncidentAt
        );
    }

    @Override
    public NodesMapResource getNodesMap(GetNodesMapQuery query, Jwt jwt) {
        Set<UUID> damIds = normalizeDamIds(query.damIds());
        Map<UUID, WireNode> scoped = loadNodesScoped(jwt, damIds);
        List<NodesMapResource.NodeMapItemResource> nodes = scoped.values().stream()
                .map(node -> new NodesMapResource.NodeMapItemResource(
                        node.id(),
                        node.name(),
                        node.latitude(),
                        node.longitude(),
                        node.status()
                ))
                .toList();
        return new NodesMapResource(nodes);
    }

    private Map<UUID, WireNode> loadNodesScoped(Jwt jwt, Set<UUID> damIds) {
        List<WireNode> list = fetchAllPages(monitoring, "/v1/nodes", jwt, new ParameterizedTypeReference<WirePage<WireNode>>() {});
        return list.stream()
                .filter(node -> damIds.isEmpty() ? false : damIds.contains(node.tailingDamId()))
                .collect(Collectors.toMap(WireNode::id, Function.identity(), (a, b) -> a));
    }

    private long countNodesWithRecentData(Jwt jwt, Map<UUID, WireNode> nodesById, OffsetDateTime from) {
        if (nodesById.isEmpty()) {
            return 0;
        }
        try {
            WireBulkStatus body = monitoring.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/nodes/bulk-status")
                            .queryParam("since", from.toString())
                            .build())
                    .headers(h -> h.setBearerAuth(jwt.getTokenValue()))
                    .retrieve()
                    .body(WireBulkStatus.class);
            if (body == null || body.recentNodeIds() == null) {
                return 0;
            }
            Set<UUID> recent = new HashSet<>(body.recentNodeIds());
            return nodesById.keySet().stream().filter(recent::contains).count();
        } catch (RestClientResponseException ex) {
            return 0;
        }
    }

    private List<WireAlert> loadAlerts(Jwt jwt, Set<UUID> damIds) {
        if (damIds.isEmpty()) {
            return List.of();
        }
        String damFilter = damIds.stream().map(UUID::toString).collect(Collectors.joining(","));
        List<WireAlert> all = new ArrayList<>();
        int page = 0;
        while (true) {
            int currentPage = page;
            WirePage<WireAlert> response = alerts.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/alerts")
                            .queryParam("damIds", damFilter)
                            .queryParam("page", currentPage)
                            .queryParam("limit", 200)
                            .build())
                    .headers(h -> h.setBearerAuth(jwt.getTokenValue()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<WirePage<WireAlert>>() {});
            if (response == null || response.content() == null) {
                break;
            }
            all.addAll(response.content());
            if (response.last()) {
                break;
            }
            page++;
        }
        return all;
    }

    private List<WirePredictiveRisk> loadPredictiveRisks(Jwt jwt) {
        try {
            WirePredictiveRisk[] body = monitoring.get()
                    .uri("/v1/analytics/predictive-risks")
                    .headers(h -> h.setBearerAuth(jwt.getTokenValue()))
                    .retrieve()
                    .body(WirePredictiveRisk[].class);
            return body == null ? List.of() : List.of(body);
        } catch (RestClientResponseException ex) {
            return List.of();
        }
    }

    private List<WireRound> loadRounds(Jwt jwt) {
        return fetchAllPages(fieldops, "/v1/rounds", jwt, new ParameterizedTypeReference<WirePage<WireRound>>() {});
    }

    private <T> List<T> fetchAllPages(
            RestClient client,
            String path,
            Jwt jwt,
            ParameterizedTypeReference<WirePage<T>> type
    ) {
        List<T> all = new ArrayList<>();
        int page = 0;
        while (true) {
            int currentPage = page;
            WirePage<T> response = client.get()
                    .uri(uriBuilder -> uriBuilder.path(path).queryParam("page", currentPage).queryParam("limit", 200).build())
                    .headers(h -> h.setBearerAuth(jwt.getTokenValue()))
                    .retrieve()
                    .body(type);
            if (response == null || response.content() == null) {
                break;
            }
            all.addAll(response.content());
            if (response.last()) {
                break;
            }
            page++;
        }
        return all;
    }

    private boolean nodeBelongsToDamScope(UUID nodeId, Set<UUID> damIds, Map<UUID, WireNode> nodesById) {
        if (damIds.isEmpty()) {
            return false;
        }
        return Optional.ofNullable(nodesById.get(nodeId))
                .map(node -> damIds.contains(node.tailingDamId()))
                .orElse(false);
    }

    private Set<UUID> normalizeDamIds(Set<UUID> damIds) {
        return damIds == null ? Set.of() : damIds;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WireNode(
            UUID id,
            String name,
            UUID tailingDamId,
            BigDecimal latitude,
            BigDecimal longitude,
            String status
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WireAlert(UUID nodeId, String status, String severity, OffsetDateTime createdAt) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WirePredictiveRisk(UUID nodeId, Long leadTimeMinutes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WireRound(UUID tailingDamId, String status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WireBulkStatus(List<UUID> recentNodeIds, long totalScopedNodes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WirePage<T>(List<T> content, boolean last) {
    }
}
