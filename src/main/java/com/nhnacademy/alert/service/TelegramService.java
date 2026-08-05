package com.nhnacademy.alert.service;

import com.nhnacademy.alert.common.config.TelegramProperties;
import com.nhnacademy.alert.dto.FiringAlertCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {

    private final DateTimeFormatter formatter;
    private final TelegramProperties telegramProperties;
    private final RestClient restClient;

    public void sendError(FiringAlertCommand command) {
        String chatUri = "https://api.telegram.org/bot" + telegramProperties.botToken();

        try {
            restClient.post()
                    .uri(chatUri + "/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(formatMessage(command))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("텔레그램 알림 전송 실패", e);
        }
    }

    private TelegramMessage formatMessage(FiringAlertCommand command) {
        String template = """
        [ERROR] %s
        %s
        %s
        """;

        String formattedTime = formatter.format(command.logTimestamp());

        String message = String.format(template, command.containerName(), formattedTime, command.logMessage());

        return new TelegramMessage(telegramProperties.chatId(), message);
    }

    private record TelegramMessage(String chat_id, String text) {}
}