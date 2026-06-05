package com.chicamax.sentinella.nodeadmin.domain.services;

import com.chicamax.sentinella.nodeadmin.domain.model.aggregates.IoTNode;
import com.chicamax.sentinella.nodeadmin.domain.model.queries.GetNodesByDamQuery;
import java.util.List;

public interface IoTNodeQueryService {
    List<IoTNode> handle(GetNodesByDamQuery query);
}
