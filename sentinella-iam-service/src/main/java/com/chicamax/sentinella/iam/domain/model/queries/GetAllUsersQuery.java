package com.chicamax.sentinella.iam.domain.model.queries;

import java.util.UUID;

public record GetAllUsersQuery(UUID organizationId, boolean systemWide) {
    public static GetAllUsersQuery forOrganization(UUID organizationId) {
        return new GetAllUsersQuery(organizationId, false);
    }

    public static GetAllUsersQuery allOrganizations() {
        return new GetAllUsersQuery(null, true);
    }
}
