package com.chicamax.sentinella.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sentinella.downstream")
public record DashboardDownstreamProperties(
        String monitoringBaseUrl,
        String alertsBaseUrl,
        String fieldopsBaseUrl
) {
}
