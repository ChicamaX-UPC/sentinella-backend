package com.chicamax.sentinella.monitoring.application.internal.prediction;

import com.chicamax.sentinella.monitoring.domain.model.aggregates.SensorNode;
import com.chicamax.sentinella.monitoring.domain.model.entities.ThresholdRule;
import com.chicamax.sentinella.monitoring.domain.model.valueobjects.SensorType;
import com.chicamax.sentinella.monitoring.domain.prediction.NodeForecastResult;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.SensorNodeRepository;
import com.chicamax.sentinella.monitoring.infrastructure.persistence.jpa.ThresholdRuleRepository;
import com.chicamax.sentinella.contracts.messaging.SentinellaMessagingConstants;
import com.chicamax.sentinella.shared.infrastructure.messaging.events.PredictiveBreachMessage;
import java.util.EnumSet;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ForecastEvaluationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ForecastEvaluationScheduler.class);
    private static final EnumSet<SensorType> FORECAST_TYPES = EnumSet.of(SensorType.WATER_LEVEL, SensorType.PRESSURE);

    private final ThresholdRuleRepository thresholdRuleRepository;
    private final SensorNodeRepository sensorNodeRepository;
    private final TrendForecastService trendForecastService;
    private final RabbitTemplate rabbitTemplate;
    private final int alertWindowHours;
    private final int horizonHours;

    public ForecastEvaluationScheduler(
            ThresholdRuleRepository thresholdRuleRepository,
            SensorNodeRepository sensorNodeRepository,
            TrendForecastService trendForecastService,
            RabbitTemplate rabbitTemplate,
            @Value("${sentinella.prediction.alert-window-hours:12}") int alertWindowHours,
            @Value("${sentinella.prediction.horizon-hours:24}") int horizonHours
    ) {
        this.thresholdRuleRepository = thresholdRuleRepository;
        this.sensorNodeRepository = sensorNodeRepository;
        this.trendForecastService = trendForecastService;
        this.rabbitTemplate = rabbitTemplate;
        this.alertWindowHours = alertWindowHours;
        this.horizonHours = horizonHours;
    }

    @Scheduled(cron = "${sentinella.prediction.scheduler-cron:0 5,20,35,50 * * * *}")
    public void evaluatePredictiveBreaches() {
        int published = 0;
        for (ThresholdRule rule : thresholdRuleRepository.findAll()) {
            if (!rule.isActive()) {
                continue;
            }
            SensorNode node = sensorNodeRepository.findById(rule.getNodeId()).orElse(null);
            if (node == null || !FORECAST_TYPES.contains(node.getSensorType())) {
                continue;
            }
            Optional<NodeForecastResult> forecast = trendForecastService.forecast(
                    rule.getNodeId(),
                    horizonHours,
                    rule.getThresholdValue(),
                    rule.getOperator()
            );
            if (forecast.isEmpty()) {
                continue;
            }
            NodeForecastResult result = forecast.get();
            if (result.estimatedThresholdBreachAt() == null || result.leadTimeMinutes() == null) {
                continue;
            }
            if (result.leadTimeMinutes() > alertWindowHours * 60L || result.leadTimeMinutes() < 0) {
                continue;
            }
            var message = PredictiveBreachMessage.fromForecast(
                    rule.getId(),
                    rule.getNodeId(),
                    result.sensorType(),
                    result.currentValue(),
                    rule.getSeverity().name(),
                    rule.getChannels(),
                    result.estimatedThresholdBreachAt(),
                    result.leadTimeMinutes()
            );
            rabbitTemplate.convertAndSend(
                    SentinellaMessagingConstants.SENTINELLA_EXCHANGE,
                    SentinellaMessagingConstants.ALERT_PREDICTIVE_TRIGGERED_ROUTING,
                    message
            );
            published++;
        }
        if (published > 0) {
            log.info("sentinella.prediction: {} alertas predictivas publicadas", published);
        }
    }
}
