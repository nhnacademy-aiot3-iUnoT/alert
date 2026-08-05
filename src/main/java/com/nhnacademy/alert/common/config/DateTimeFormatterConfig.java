package com.nhnacademy.alert.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class DateTimeFormatterConfig {

    private final TimeProperties timeProperties;

    @Bean
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter.ofPattern(timeProperties.format())
                .withZone(ZoneId.of(timeProperties.zoneId()));
    }
}
