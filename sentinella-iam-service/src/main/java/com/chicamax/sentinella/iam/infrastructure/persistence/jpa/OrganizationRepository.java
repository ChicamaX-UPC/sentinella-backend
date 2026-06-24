package com.chicamax.sentinella.iam.infrastructure.persistence.jpa;

import com.chicamax.sentinella.iam.domain.model.aggregates.Organization;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
}
