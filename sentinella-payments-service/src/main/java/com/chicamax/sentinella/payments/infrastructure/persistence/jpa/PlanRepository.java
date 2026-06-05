package com.chicamax.sentinella.payments.infrastructure.persistence.jpa;

import com.chicamax.sentinella.payments.domain.model.aggregates.Plan;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
}
