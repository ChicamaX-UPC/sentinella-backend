package com.chicamax.sentinella.dashboard.application.internal.queryservices;

import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertSeverity;
import com.chicamax.sentinella.alerts.domain.model.valueobjects.AlertStatus;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.chicamax.sentinella.dashboard.domain.model.queries.GetExecutiveDashboardQuery;
import com.chicamax.sentinella.dashboard.domain.model.queries.GetFieldDashboardQuery;
import com.chicamax.sentinella.dashboard.domain.model.queries.GetNodesMapQuery;
import com.chicamax.sentinella.dashboard.domain.services.DashboardQueryService;
import com.chicamax.sentinella.dashboard.interfaces.rest.resources.ExecutiveDashboardResource;
import com.chicamax.sentinella.dashboard.interfaces.rest.resources.FieldDashboardResource;
import com.chicamax.sentinella.dashboard.interfaces.rest.resources.NodesMapResource;
import com.chicamax.sentinella.fieldoperations.domain.model.valueobjects.RoundStatus;
import com.chicamax.sentinella.fieldoperations.infrastructure.persistence.jpa.InspectionRoundRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorReadingRepository;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DashboardQueryServiceImpl implements DashboardQueryService {

    private final SensorNodeRepository sensorNodeRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final AlertRepository alertRepository;
    private final InspectionRoundRepository inspectionRoundRepository;

    public DashboardQueryServiceImpl(
            SensorNodeRepository sensorNodeRepository,
            SensorReadingRepository sensorReadingRepository,
            AlertRepository alertRepository,
            InspectionRoundRepository inspectionRoundRepository
    ) {
        this.sensorNodeRepository = sensorNodeRepository;
        this.sensorReadingRepository = sensorReadingRepository;
        this.alertRepository = alertRepository;
        this.inspectionRoundRepository = inspectionRoundRepository;
    }

    @Override
    public ExecutiveDashboardResource getExecutive(GetExecutiveDashboardQuery query) {
        Set<UUID> damIds = normalizeDamIds(query.damIds());
        var scopedNodes = sensorNodeRepository.findAll().stream()
                .filter(node -> damIds.isEmpty() || damIds.contains(node.getTailingDamId()))
                .toList();
        long totalNodes = scopedNodes.size();
        long activeAlerts = alertRepository.search(AlertStatus.ACTIVE, null, null).stream()
                .filter(alert -> nodeBelongsToDamScope(alert.getNodeId(), damIds))
                .count();
        long criticalAlerts = alertRepository.search(null, AlertSeverity.CRITICAL, null).stream()
                .filter(alert -> nodeBelongsToDamScope(alert.getNodeId(), damIds))
                .count();
        OffsetDateTime from = OffsetDateTime.now().minusHours(1);
        long nodesWithRecentData = scopedNodes.stream()
                .filter(node -> sensorReadingRepository
                        .findTopByNodeIdAndTimestampGreaterThanEqualOrderByTimestampDesc(node.getId(), from)
                        .isPresent())
                .count();
        return new ExecutiveDashboardResource(totalNodes, activeAlerts, criticalAlerts, nodesWithRecentData);
    }

    @Override
    public FieldDashboardResource getField(GetFieldDashboardQuery query) {
        Set<UUID> damIds = normalizeDamIds(query.damIds());
        long activeAlerts = alertRepository.search(AlertStatus.ACTIVE, null, null).stream()
                .filter(alert -> nodeBelongsToDamScope(alert.getNodeId(), damIds))
                .count();
        long roundsInProgress = inspectionRoundRepository.findAll().stream()
                .filter(round -> damIds.isEmpty() || damIds.contains(round.getTailingDamId()))
                .filter(round -> round.getStatus() == RoundStatus.IN_PROGRESS)
                .count();
        long pendingSync = inspectionRoundRepository.findAll().stream()
                .filter(round -> damIds.isEmpty() || damIds.contains(round.getTailingDamId()))
                .filter(round -> round.getStatus() == RoundStatus.COMPLETED)
                .count();
        return new FieldDashboardResource(activeAlerts, roundsInProgress, pendingSync);
    }

    @Override
    public NodesMapResource getNodesMap(GetNodesMapQuery query) {
        Set<UUID> damIds = normalizeDamIds(query.damIds());
        var nodes = sensorNodeRepository.findAll().stream()
                .filter(node -> damIds.isEmpty() || damIds.contains(node.getTailingDamId()))
                .map(node -> new NodesMapResource.NodeMapItemResource(
                        node.getId(),
                        node.getName(),
                        node.getLatitude(),
                        node.getLongitude(),
                        node.getStatus()
                ))
                .toList();
        return new NodesMapResource(nodes);
    }

    private boolean nodeBelongsToDamScope(UUID nodeId, Set<UUID> damIds) {
        if (damIds.isEmpty()) {
            return true;
        }
        return sensorNodeRepository.findById(nodeId)
                .map(node -> damIds.contains(node.getTailingDamId()))
                .orElse(false);
    }

    private Set<UUID> normalizeDamIds(Set<UUID> damIds) {
        return damIds == null ? Set.of() : damIds;
    }
}
