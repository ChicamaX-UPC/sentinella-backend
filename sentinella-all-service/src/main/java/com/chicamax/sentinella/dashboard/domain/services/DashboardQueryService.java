package com.chicamax.sentinella.dashboard.domain.services;

import com.chicamax.sentinella.dashboard.interfaces.rest.resources.ExecutiveDashboardResource;
import com.chicamax.sentinella.dashboard.interfaces.rest.resources.FieldDashboardResource;
import com.chicamax.sentinella.dashboard.interfaces.rest.resources.NodesMapResource;
import com.chicamax.sentinella.dashboard.domain.model.queries.GetExecutiveDashboardQuery;
import com.chicamax.sentinella.dashboard.domain.model.queries.GetFieldDashboardQuery;
import com.chicamax.sentinella.dashboard.domain.model.queries.GetNodesMapQuery;

public interface DashboardQueryService {
    ExecutiveDashboardResource getExecutive(GetExecutiveDashboardQuery query);

    FieldDashboardResource getField(GetFieldDashboardQuery query);

    NodesMapResource getNodesMap(GetNodesMapQuery query);
}
