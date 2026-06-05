package com.chicamax.sentinella.monitoring.application.internal.commandservices;

import com.chicamax.sentinella.monitoring.domain.model.commands.CreateThresholdRuleCommand;
import com.chicamax.sentinella.monitoring.domain.model.commands.UpdateThresholdRuleCommand;
import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import com.chicamax.sentinella.monitoring.domain.services.ThresholdRuleCommandService;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ThresholdRuleRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ThresholdRuleCommandServiceImpl implements ThresholdRuleCommandService {

    private final ThresholdRuleRepository thresholdRuleRepository;

    public ThresholdRuleCommandServiceImpl(ThresholdRuleRepository thresholdRuleRepository) {
        this.thresholdRuleRepository = thresholdRuleRepository;
    }

    @Override
    @Transactional
    public ThresholdRule create(CreateThresholdRuleCommand command) {
        ThresholdRule rule = new ThresholdRule(
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
        return thresholdRuleRepository.save(rule);
    }

    @Override
    @Transactional
    public ThresholdRule update(UpdateThresholdRuleCommand command) {
        ThresholdRule rule = thresholdRuleRepository.findById(command.ruleId())
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
        return thresholdRuleRepository.save(rule);
    }

    @Override
    @Transactional
    public void delete(UUID ruleId) {
        if (!thresholdRuleRepository.existsById(ruleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla no encontrada");
        }
        thresholdRuleRepository.deleteById(ruleId);
    }
}
