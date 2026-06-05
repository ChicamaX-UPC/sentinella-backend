package com.chicamax.sentinella.dashboard.application.internal.queryservices;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

    public DashboardQueryServiceImpl(
            DashboardDownstreamProperties props,
            @Value("${sentinella.dashboard.recent-data-hours:1}") int recentDataHours
    ) {
        this.monitoring = RestClient.builder().baseUrl(props.monitoringBaseUrl()).build();
        this.alerts = RestClient.builder().baseUrl(props.alertsBaseUrl()).build();
        this.fieldops = RestClient.builder().baseUrl(props.fieldopsBaseUrl()).build();
        this.recentDataHours = Math.max(1, recentDataHours);
    }

    @Override
    public ExecutiveDashboardResource getExecutive(GetExecutiveDashboardQuery query, Jwt jwt) {
        Set<UUID> damIds = normalizeDamIds(query.damIds());
        Map<UUID, WireNode> nodesById = loadNodesScoped(jwt, damIds);
        List<WireAlert> alertList = loadAlerts(jwt);
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

        long nodesWithRecentData = nodesById.values().stream()
                .filter(node -> readingIsRecent(jwt, node.id(), from))
                .count();

        return new ExecutiveDashboardResource(totalNodes, activeAlerts, criticalAlerts, nodesWithRecentData);
    }

    @Override
    public FieldDashboardResource getField(GetFieldDashboardQuery query, Jwt jwt) {
        Set<UUID> damIds = normalizeDamIds(query.damIds());
        Map<UUID, WireNode> nodesById = loadNodesScoped(jwt, damIds);
        List<WireAlert> alertList = loadAlerts(jwt);
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
        return new FieldDashboardResource(activeAlerts, roundsInProgress, pendingSync);
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
                .filter(node -> damIds.isEmpty() || damIds.contains(node.tailingDamId()))
                .collect(Collectors.toMap(WireNode::id, Function.identity(), (a, b) -> a));
    }

    private List<WireAlert> loadAlerts(Jwt jwt) {
        return fetchAllPages(alerts, "/v1/alerts", jwt, new ParameterizedTypeReference<WirePage<WireAlert>>() {});
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

    private boolean readingIsRecent(Jwt jwt, UUID nodeId, OffsetDateTime from) {
        try {
            WireReadingLatest reading = monitoring.get()
                    .uri("/v1/nodes/{nodeId}/status", nodeId)
                    .headers(h -> h.setBearerAuth(jwt.getTokenValue()))
                    .retrieve()
                    .body(WireReadingLatest.class);
            return reading != null && reading.timestamp != null && !reading.timestamp.isBefore(from);
        } catch (RestClientResponseException ex) {
            HttpStatusCode code = ex.getStatusCode();
            if (code.equals(HttpStatus.NOT_FOUND)) {
                return false;
            }
            throw ex;
        }
    }

    private boolean nodeBelongsToDamScope(UUID nodeId, Set<UUID> damIds, Map<UUID, WireNode> nodesById) {
        if (damIds.isEmpty()) {
            return true;
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
    private record WireAlert(UUID nodeId, String status, String severity) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WireRound(UUID tailingDamId, String status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WireReadingLatest(OffsetDateTime timestamp) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WirePage<T>(List<T> content, boolean last) {
    }
}
