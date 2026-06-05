package com.chicamax.sentinella.alerts.application.internal.queryservices;

import com.chicamax.sentinella.alerts.domain.model.entities.AlertRule;
import com.chicamax.sentinella.alerts.domain.services.AlertRuleQueryService;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRuleRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AlertRuleQueryServiceImpl implements AlertRuleQueryService {

    private final AlertRuleRepository alertRuleRepository;

    public AlertRuleQueryServiceImpl(AlertRuleRepository alertRuleRepository) {
        this.alertRuleRepository = alertRuleRepository;
    }

    @Override
    public List<AlertRule> getAll() {
        return alertRuleRepository.findAll();
    }

    @Override
    public Optional<AlertRule> findById(UUID ruleId) {
        return alertRuleRepository.findById(ruleId);
    }
}
