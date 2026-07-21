package com.nhnacademy.alert.controller;

import com.nhnacademy.alert.dto.GrafanaWebhookPayload;
import com.nhnacademy.alert.dto.LogEntry;
import com.nhnacademy.alert.service.LogSearchService;
import com.nhnacademy.alert.service.TelegramNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GrafanaWebhookController {

    private final LogSearchService logSearchService;
    private final TelegramNotifier telegramNotifier;

    @PostMapping("/webhook/grafana-alert")
    public void handleAlert(@RequestBody String payload) {
        log.info("Payload: {}", payload);
//
//        if (payload.alerts() == null) {
//            return;
//        }
//
//        for (GrafanaWebhookPayload.Alert alert : payload.alerts()) {
//            if (!"firing".equals(alert.status())) {
//                continue;
//            }
//
//            String containerName = alert.labels() != null ? alert.labels().get("container_name") : null;
//            Instant from = Instant.parse(alert.startsAt());
//
//            List<LogEntry> entries = logSearchService.searchErrors(containerName, from, Instant.now());
//            telegramNotifier.sendErrorAlert(containerName != null ? containerName : "unknown", entries);
//        }
    }
}
