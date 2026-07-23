package com.nhnacademy.alert.service;

import com.nhnacademy.alert.config.TelegramProperties;
import com.nhnacademy.alert.dto.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class TelegramService {
    private static final String TEMPLATE = """
        ERROR [%s]

        %s
        %s
        %s
    """;

    private final RestClient restClient;
    private final String chatId;

    public TelegramService(TelegramProperties properties) {
        this.chatId = properties.chatId();
        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + properties.botToken())
                .build();
    }

    public void sendSingleError(String containerName, LogEntry entry) {
        try {
            restClient.post()
                    .uri("/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new TelegramMessage(chatId, formatSingle(containerName, entry)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("텔레그램 알림 전송 실패", e);
        }
    }

    private String formatSingle(String containerName, LogEntry entry) {
        return String.format(TEMPLATE, containerName, entry.timestamp(), entry.logger(), entry.message());
    }

    private record TelegramMessage(String chat_id, String text) {}
}
