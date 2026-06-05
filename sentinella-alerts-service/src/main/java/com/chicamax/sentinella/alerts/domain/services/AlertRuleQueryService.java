package com.chicamax.sentinella.alerts.domain.services;

import com.chicamax.sentinella.alerts.domain.model.entities.AlertRule;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlertRuleQueryService {
    List<AlertRule> getAll();

    Optional<AlertRule> findById(UUID ruleId);
}
