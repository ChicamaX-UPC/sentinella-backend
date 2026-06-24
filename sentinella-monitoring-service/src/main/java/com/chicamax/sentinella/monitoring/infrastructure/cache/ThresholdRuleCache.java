package com.chicamax.sentinella.monitoring.infrastructure.cache;

import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ThresholdRuleRepository;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ThresholdRuleCache {

    private final LoadingCache<UUID, List<ThresholdRule>> cache;

    public ThresholdRuleCache(
            ThresholdRuleRepository thresholdRuleRepository,
            @Value("${sentinella.monitoring.threshold-rule-cache-seconds:60}") long ttlSeconds
    ) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(Math.max(10, ttlSeconds)))
                .maximumSize(10_000)
                .build(nodeId -> thresholdRuleRepository.findByNodeIdAndActiveTrue(nodeId));
    }

    public List<ThresholdRule> activeRulesForNode(UUID nodeId) {
        return cache.get(nodeId);
    }

    public void invalidateNode(UUID nodeId) {
        if (nodeId != null) {
            cache.invalidate(nodeId);
        }
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }
}
