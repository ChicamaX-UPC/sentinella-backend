package com.chicamax.sentinella.monitoring.interfaces.rest.transform;

import com.chicamax.sentinella.monitoring.domain.model.commands.CreateThresholdRuleCommand;
import com.chicamax.sentinella.monitoring.domain.model.commands.UpdateThresholdRuleCommand;
import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.AlertRuleResource;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.CreateAlertRuleResource;
import com.chicamax.sentinella.monitoring.interfaces.rest.resources.UpdateAlertRuleResource;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AlertRuleAssembler {

    public CreateThresholdRuleCommand toCommand(CreateAlertRuleResource resource, UUID actorId) {
        return new CreateThresholdRuleCommand(
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

    public UpdateThresholdRuleCommand toCommand(UUID ruleId, UpdateAlertRuleResource resource, UUID actorId) {
        return new UpdateThresholdRuleCommand(
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

    public AlertRuleResource toResource(ThresholdRule rule) {
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
