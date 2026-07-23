package com.nhnacademy.alert.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "es")
public record ElasticsearchProperties(
        String host,
        String user,
        String password,
        String indexPattern,
        String logLevel
) {
}
