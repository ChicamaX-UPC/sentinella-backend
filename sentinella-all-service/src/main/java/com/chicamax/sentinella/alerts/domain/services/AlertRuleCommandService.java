package com.chicamax.sentinella.alerts.domain.services;

import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertRuleCommand;
import com.chicamax.sentinella.alerts.domain.model.commands.UpdateAlertRuleCommand;
import com.chicamax.sentinella.alerts.domain.model.entities.AlertRule;
import java.util.UUID;

public interface AlertRuleCommandService {
    AlertRule create(CreateAlertRuleCommand command);

    AlertRule update(UpdateAlertRuleCommand command);

    void delete(UUID ruleId);
}
