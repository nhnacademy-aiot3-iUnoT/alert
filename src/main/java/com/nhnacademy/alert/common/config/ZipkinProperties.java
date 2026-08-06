package com.nhnacademy.alert.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zipkin")
public record ZipkinProperties(
        String baseUrl
) {}
