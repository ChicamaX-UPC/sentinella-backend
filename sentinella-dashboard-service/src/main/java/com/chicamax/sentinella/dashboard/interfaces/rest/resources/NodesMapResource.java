package com.chicamax.sentinella.dashboard.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record NodesMapResource(List<NodeMapItemResource> nodes) {
    public record NodeMapItemResource(
            UUID nodeId,
            String name,
            BigDecimal latitude,
            BigDecimal longitude,
            String status
    ) {
    }
}
