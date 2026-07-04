package com.chicamax.sentinella.iam.interfaces.rest.resources;

import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import java.util.UUID;

public record OrganizationMemberResource(
        UUID id,
        String fullName,
        String email,
        Role role,
        String jobTitle,
        boolean active
) {
}
