package com.chicamax.sentinella.alerts.infrastructure.persistence.jpa;

import com.chicamax.sentinella.alerts.domain.model.entities.AlertRule;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRuleRepository extends JpaRepository<AlertRule, UUID> {
}
