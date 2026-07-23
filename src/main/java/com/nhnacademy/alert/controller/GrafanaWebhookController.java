package com.nhnacademy.alert.controller;

import com.nhnacademy.alert.dto.GrafanaWebhookRequest;
import com.nhnacademy.alert.usecase.TelegramNotifyUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GrafanaWebhookController {
    private final TelegramNotifyUseCase telegramNotifyUseCase;

    @PostMapping("/webhook/grafana-alert")
    public void handleAlert(@RequestBody GrafanaWebhookRequest request) {
        telegramNotifyUseCase.execute(request.toCommand());
    }
}