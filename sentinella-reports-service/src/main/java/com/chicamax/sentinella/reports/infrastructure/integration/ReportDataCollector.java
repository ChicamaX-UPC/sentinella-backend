package com.chicamax.sentinella.reports.infrastructure.integration;

import com.chicamax.sentinella.reports.config.ReportDownstreamProperties;
import com.chicamax.sentinella.reports.domain.model.commands.GenerateReportCommand;
import com.chicamax.sentinella.reports.domain.model.valueobjects.ReportType;
import com.chicamax.sentinella.shared.bootstrap.demo.SentinellaDemoIds;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ReportDataCollector {

    private final RestClient monitoring;
    private final RestClient alerts;
    private final RestClient fieldops;

    public ReportDataCollector(ReportDownstreamProperties props) {
        this.monitoring = RestClient.builder().baseUrl(props.monitoringBaseUrl()).build();
        this.alerts = RestClient.builder().baseUrl(props.alertsBaseUrl()).build();
        this.fieldops = RestClient.builder().baseUrl(props.fieldopsBaseUrl()).build();
    }

    public ReportDataset collect(GenerateReportCommand command, String bearerToken) {
        UUID damId = command.tailingDamId() != null
                ? command.tailingDamId()
                : SentinellaDemoIds.TAILING_DAM_CHICAMA_NORTE;

        List<WireNode> nodes = loadNodes(bearerToken).stream()
                .filter(n -> damId.equals(n.tailingDamId()))
                .toList();

        Map<UUID, WireNode> nodesById = nodes.stream()
                .collect(Collectors.toMap(WireNode::id, Function.identity(), (a, b) -> a));

        List<ReportDataset.NodeLine> nodeLines = nodes.stream()
                .map(n -> new ReportDataset.NodeLine(
                        n.id(),
                        n.externalId(),
                        n.name(),
                        n.sensorType() != null ? n.sensorType() : "—",
                        n.status() != null ? n.status() : "—"
                ))
                .toList();

        List<ReportDataset.ReadingStatsLine> stats = new ArrayList<>();
        if (command.type() == ReportType.REGULATORY_OEFA || command.type() == ReportType.INSPECTION_SUMMARY) {
            for (WireNode node : nodes) {
                List<WireReading> readings = loadReadings(bearerToken, node.id(), command.from(), command.to());
                stats.add(toStats(node, readings));
            }
            stats.sort(Comparator.comparing(ReportDataset.ReadingStatsLine::nodeLabel));
        }

        List<ReportDataset.AlertLine> alertLines = List.of();
        if (command.type() == ReportType.ALERT_HISTORY || command.type() == ReportType.REGULATORY_OEFA) {
            alertLines = loadAlerts(bearerToken).stream()
                    .filter(a -> nodesById.containsKey(a.nodeId()))
                    .map(a -> new ReportDataset.AlertLine(
                            a.id(),
                            a.nodeId(),
                            a.sensorType(),
                            a.triggeredValue(),
                            a.severity() != null ? a.severity() : "—",
                            a.status() != null ? a.status() : "—"
                    ))
                    .limit(120)
                    .toList();
        }

        List<ReportDataset.RoundLine> roundLines = List.of();
        if (command.type() == ReportType.INSPECTION_SUMMARY || command.type() == ReportType.REGULATORY_OEFA) {
            roundLines = loadRounds(bearerToken).stream()
                    .filter(r -> damId.equals(r.tailingDamId()))
                    .filter(r -> inRange(r.scheduledAt(), command.from(), command.to()))
                    .map(r -> new ReportDataset.RoundLine(
                            r.id(),
                            r.scheduledAt(),
                            r.completedAt(),
                            r.status() != null ? r.status() : "—",
                            r.operatorId()
                    ))
                    .sorted(Comparator.comparing(ReportDataset.RoundLine::scheduledAt).reversed())
                    .limit(80)
                    .toList();
        }

        return new ReportDataset(
                damId,
                "Tranque Chicama Norte",
                command.from(),
                command.to(),
                nodeLines,
                stats,
                alertLines,
                roundLines
        );
    }

    private List<WireNode> loadNodes(String bearerToken) {
        return fetchAllPages(monitoring, "/v1/nodes", bearerToken, new ParameterizedTypeReference<WirePage<WireNode>>() {});
    }

    private List<WireReading> loadReadings(
            String bearerToken,
            UUID nodeId,
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        List<WireReading> all = new ArrayList<>();
        int page = 0;
        while (true) {
            int currentPage = page;
            WirePage<WireReading> response = monitoring.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/nodes/{nodeId}/readings")
                            .queryParam("from", from)
                            .queryParam("to", to)
                            .queryParam("page", currentPage)
                            .queryParam("limit", 200)
                            .build(nodeId))
                    .headers(h -> h.setBearerAuth(bearerToken))
                    .retrieve()
                    .body(new ParameterizedTypeReference<WirePage<WireReading>>() {});
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

    private List<WireAlert> loadAlerts(String bearerToken) {
        return fetchAllPages(alerts, "/v1/alerts", bearerToken, new ParameterizedTypeReference<WirePage<WireAlert>>() {});
    }

    private List<WireRound> loadRounds(String bearerToken) {
        return fetchAllPages(fieldops, "/v1/rounds", bearerToken, new ParameterizedTypeReference<WirePage<WireRound>>() {});
    }

    private <T> List<T> fetchAllPages(
            RestClient client,
            String path,
            String bearerToken,
            ParameterizedTypeReference<WirePage<T>> type
    ) {
        List<T> all = new ArrayList<>();
        int page = 0;
        while (true) {
            int currentPage = page;
            WirePage<T> response = client.get()
                    .uri(uriBuilder -> uriBuilder.path(path).queryParam("page", currentPage).queryParam("limit", 200).build())
                    .headers(h -> h.setBearerAuth(bearerToken))
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

    private static ReportDataset.ReadingStatsLine toStats(WireNode node, List<WireReading> readings) {
        if (readings.isEmpty()) {
            return new ReportDataset.ReadingStatsLine(
                    label(node),
                    node.sensorType() != null ? node.sensorType() : "—",
                    0,
                    null,
                    null,
                    null,
                    "—",
                    "—",
                    null
            );
        }
        BigDecimal min = readings.stream().map(WireReading::value).filter(Objects::nonNull).min(BigDecimal::compareTo).orElse(null);
        BigDecimal max = readings.stream().map(WireReading::value).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(null);
        BigDecimal sum = readings.stream()
                .map(WireReading::value)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avg = sum.divide(BigDecimal.valueOf(readings.size()), 2, RoundingMode.HALF_UP);
        WireReading latest = readings.stream()
                .max(Comparator.comparing(WireReading::timestamp, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(readings.getFirst());
        return new ReportDataset.ReadingStatsLine(
                label(node),
                node.sensorType() != null ? node.sensorType() : "—",
                readings.size(),
                min,
                max,
                avg,
                latest.unit() != null ? latest.unit() : "—",
                latest.status() != null ? latest.status() : "—",
                latest.timestamp()
        );
    }

    private static String label(WireNode node) {
        return node.externalId() + " — " + node.name();
    }

    private static boolean inRange(OffsetDateTime value, OffsetDateTime from, OffsetDateTime to) {
        if (value == null) {
            return false;
        }
        return !value.isBefore(from) && !value.isAfter(to);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WireNode(
            UUID id,
            String externalId,
            String name,
            UUID tailingDamId,
            String sensorType,
            String status
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WireReading(
            OffsetDateTime timestamp,
            BigDecimal value,
            String unit,
            String status
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WireAlert(
            UUID id,
            UUID nodeId,
            String sensorType,
            BigDecimal triggeredValue,
            String severity,
            String status
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WirePage<T>(List<T> content, boolean last) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WireRound(
            UUID id,
            UUID operatorId,
            UUID tailingDamId,
            OffsetDateTime scheduledAt,
            OffsetDateTime completedAt,
            String status
    ) {
    }
}
