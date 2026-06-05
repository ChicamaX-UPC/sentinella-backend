package com.chicamax.sentinella.reports.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sentinella.downstream")
public record ReportDownstreamProperties(
        String monitoringBaseUrl,
        String alertsBaseUrl,
        String fieldopsBaseUrl
) {
}
