package com.chicamax.sentinella.monitoring.domain.services;

import com.chicamax.sentinella.monitoring.domain.model.commands.CreateThresholdRuleCommand;
import com.chicamax.sentinella.monitoring.domain.model.commands.UpdateThresholdRuleCommand;
import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import java.util.UUID;

public interface ThresholdRuleCommandService {
    ThresholdRule create(CreateThresholdRuleCommand command);

    ThresholdRule update(UpdateThresholdRuleCommand command);

    void delete(UUID ruleId);
}
