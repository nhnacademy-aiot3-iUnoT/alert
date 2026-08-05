package com.nhnacademy.alert.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "time")
public record TimeProperties(
        String zoneId,
        String format
) {}
