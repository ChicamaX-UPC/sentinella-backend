package com.chicamax.sentinella.reports.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ReportDownstreamProperties.class)
public class ReportDownstreamConfig {
}
