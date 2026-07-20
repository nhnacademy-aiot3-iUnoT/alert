package com.nhnacademy.alert.service;

import com.nhnacademy.alert.dto.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
public class TelegramNotifier {

    private final RestClient restClient;
    private final String chatId;

    public TelegramNotifier(
            @Value("${telegram.bot-token}") String botToken,
            @Value("${telegram.chat-id}") String chatId
    ) {
        this.chatId = chatId;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + botToken)
                .build();
    }

    public void sendErrorAlert(String serviceLabel, List<LogEntry> entries) {
        try {
            restClient.post()
                    .uri("/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new TelegramMessage(chatId, formatMessage(serviceLabel, entries)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("텔레그램 알림 전송 실패", e);
        }
    }

    private String formatMessage(String serviceLabel, List<LogEntry> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚨 ERROR 발생 [").append(serviceLabel).append("]\n\n");

        if (entries.isEmpty()) {
            sb.append("상세 로그를 찾지 못했습니다. Kibana에서 직접 확인해주세요.");
            return sb.toString();
        }

        for (LogEntry entry : entries) {
            sb.append(entry.timestamp()).append('\n')
                    .append(entry.containerName()).append(" / ").append(entry.logger()).append('\n')
                    .append(entry.message()).append("\n\n");
        }
        return sb.toString();
    }

    private record TelegramMessage(String chat_id, String text) {
    }
}
