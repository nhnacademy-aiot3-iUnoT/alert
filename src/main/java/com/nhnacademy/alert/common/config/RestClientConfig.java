package com.nhnacademy.alert.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class RestClientConfig {

    @Bean
    public RestClient telegramRestClient(TelegramProperties telegramProperties) {
        return RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + telegramProperties.botToken())
                .build();
    }
}
