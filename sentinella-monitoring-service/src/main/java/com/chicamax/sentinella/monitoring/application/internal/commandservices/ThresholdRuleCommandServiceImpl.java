package com.chicamax.sentinella.monitoring.application.internal.commandservices;

import com.chicamax.sentinella.monitoring.domain.model.commands.CreateThresholdRuleCommand;
import com.chicamax.sentinella.monitoring.domain.model.commands.UpdateThresholdRuleCommand;
import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import com.chicamax.sentinella.monitoring.domain.services.ThresholdRuleCommandService;
import com.chicamax.sentinella.monitoring.infrastructure.cache.ThresholdRuleCache;
import com.chicamax.sentinella.monitoring.infrastructure.messaging.AlertRuleSyncPublisher;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ThresholdRuleRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ThresholdRuleCommandServiceImpl implements ThresholdRuleCommandService {

    private final ThresholdRuleRepository thresholdRuleRepository;
    private final ThresholdRuleCache thresholdRuleCache;
    private final AlertRuleSyncPublisher alertRuleSyncPublisher;

    public ThresholdRuleCommandServiceImpl(
            ThresholdRuleRepository thresholdRuleRepository,
            ThresholdRuleCache thresholdRuleCache,
            AlertRuleSyncPublisher alertRuleSyncPublisher
    ) {
        this.thresholdRuleRepository = thresholdRuleRepository;
        this.thresholdRuleCache = thresholdRuleCache;
        this.alertRuleSyncPublisher = alertRuleSyncPublisher;
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
        ThresholdRule saved = thresholdRuleRepository.save(rule);
        thresholdRuleCache.invalidateNode(saved.getNodeId());
        alertRuleSyncPublisher.publish(saved);
        return saved;
    }

    @Override
    @Transactional
    public ThresholdRule update(UpdateThresholdRuleCommand command) {
        ThresholdRule rule = thresholdRuleRepository.findById(command.ruleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla no encontrada"));
        UUID previousNodeId = rule.getNodeId();
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
        ThresholdRule saved = thresholdRuleRepository.save(rule);
        thresholdRuleCache.invalidateNode(previousNodeId);
        if (!previousNodeId.equals(saved.getNodeId())) {
            thresholdRuleCache.invalidateNode(saved.getNodeId());
        }
        alertRuleSyncPublisher.publish(saved);
        return saved;
    }

    @Override
    @Transactional
    public void delete(UUID ruleId) {
        ThresholdRule rule = thresholdRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regla no encontrada"));
        alertRuleSyncPublisher.publishDeactivated(rule);
        thresholdRuleRepository.deleteById(ruleId);
        thresholdRuleCache.invalidateNode(rule.getNodeId());
    }
}
