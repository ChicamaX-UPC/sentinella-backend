package com.chicamax.sentinella.alerts.interfaces.rest.transform;

import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertRuleCommand;
import com.chicamax.sentinella.alerts.domain.model.commands.UpdateAlertRuleCommand;
import com.chicamax.sentinella.alerts.domain.model.entities.AlertRule;
import com.chicamax.sentinella.alerts.interfaces.rest.resources.AlertRuleResource;
import com.chicamax.sentinella.alerts.interfaces.rest.resources.CreateAlertRuleResource;
import com.chicamax.sentinella.alerts.interfaces.rest.resources.UpdateAlertRuleResource;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AlertRuleAssembler {

    public CreateAlertRuleCommand toCommand(CreateAlertRuleResource resource, UUID actorId) {
        return new CreateAlertRuleCommand(
                resource.nodeId(),
                resource.sensorType(),
                resource.operator(),
                resource.thresholdValue(),
                resource.severity(),
                resource.channels(),
                resource.escalationMinutes(),
                actorId
        );
    }

    public UpdateAlertRuleCommand toCommand(UUID ruleId, UpdateAlertRuleResource resource, UUID actorId) {
        return new UpdateAlertRuleCommand(
                ruleId,
                resource.sensorType(),
                resource.operator(),
                resource.thresholdValue(),
                resource.severity(),
                resource.channels(),
                resource.escalationMinutes(),
                resource.active(),
                actorId
        );
    }

    public AlertRuleResource toResource(AlertRule rule) {
        return new AlertRuleResource(
                rule.getId(),
                rule.getNodeId(),
                rule.getSensorType(),
                rule.getOperator(),
                rule.getThresholdValue(),
                rule.getSeverity(),
                rule.decodeChannels(),
                rule.getEscalationMinutes(),
                rule.isActive(),
                rule.getUpdatedBy(),
                rule.getUpdatedAt()
        );
    }
}
