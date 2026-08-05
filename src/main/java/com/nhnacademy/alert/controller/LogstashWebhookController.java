package com.nhnacademy.alert.controller;

import com.nhnacademy.alert.dto.ErrorLogRequest;
import com.nhnacademy.alert.usecase.TelegramNotifyUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LogstashWebhookController {

    private final TelegramNotifyUseCase telegramNotifyUseCase;

    @PostMapping("/webhook/logstash")
    public void handleErrorLog(@RequestBody ErrorLogRequest request) {
        log.info("Logstash 페이로드: {}", String.valueOf(request));

        telegramNotifyUseCase.execute(request.toCommand());
    }
}
