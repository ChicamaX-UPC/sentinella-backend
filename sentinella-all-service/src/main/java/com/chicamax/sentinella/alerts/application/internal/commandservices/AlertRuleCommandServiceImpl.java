package com.chicamax.sentinella.alerts.application.internal.commandservices;

import com.chicamax.sentinella.alerts.domain.model.commands.CreateAlertRuleCommand;
import com.chicamax.sentinella.alerts.domain.model.commands.UpdateAlertRuleCommand;
import com.chicamax.sentinella.alerts.domain.model.entities.AlertRule;
import com.chicamax.sentinella.alerts.domain.services.AlertRuleCommandService;
import com.chicamax.sentinella.alerts.infrastructure.persistence.jpa.AlertRuleRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AlertRuleCommandServiceImpl implements AlertRuleCommandService {

    private final AlertRuleRepository alertRuleRepository;

    public AlertRuleCommandServiceImpl(AlertRuleRepository alertRuleRepository) {
        this.alertRuleRepository = alertRuleRepository;
    }

    @Override
    @Transactional
    public AlertRule create(CreateAlertRuleCommand command) {
        AlertRule rule = new AlertRule(
                UUID.randomUUID(),
                command.nodeId(),
                command.sensorType(),
                command.operator(),
                command.thresholdValue(),
                command.severity(),
                command.channels(),
                command.escalationMinutes(),
                command.updatedBy()
        );
        return alertRuleRepository.save(rule);
    }

    @Override
    @Transactional
    public AlertRule update(UpdateAlertRuleCommand command) {
        AlertRule rule = alertRuleRepository.findById(command.ruleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla no encontrada"));
        rule.update(
                command.sensorType(),
                command.operator(),
                command.thresholdValue(),
                command.severity(),
                command.channels(),
                command.escalationMinutes(),
                command.active(),
                command.updatedBy()
        );
        return alertRuleRepository.save(rule);
    }

    @Override
    @Transactional
    public void delete(UUID ruleId) {
        if (!alertRuleRepository.existsById(ruleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla no encontrada");
        }
        alertRuleRepository.deleteById(ruleId);
    }
}
