package com.nhnacademy.alert.service;

import com.nhnacademy.alert.config.TelegramProperties;
import com.nhnacademy.alert.dto.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class TelegramService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String TEMPLATE = """
        ERROR 로그 발생

        서비스: %s
        시간: %s
        로거: %s
        메시지: %s
        """;

    // 스택트레이스 등 여러 줄인 경우 첫 줄(실제 예외 요약)만 보여줌, 이 길이는 그 첫 줄이 비정상적으로 길 때의 안전장치
    private static final int MAX_MESSAGE_LENGTH = 300;
    private static final String TRUNCATED_SUFFIX = "\n... (생략됨, Kibana에서 전체 로그 확인)";

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
        String time = TIME_FORMATTER.format(entry.timestamp().atZone(KST));
        return String.format(TEMPLATE, containerName, time, entry.logger().strip(), truncate(entry.message()));
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }

        int newlineIndex = message.indexOf('\n');
        String firstLine = newlineIndex >= 0 ? message.substring(0, newlineIndex) : message;
        boolean wasTruncated = newlineIndex >= 0 || message.length() > MAX_MESSAGE_LENGTH;

        if (firstLine.length() > MAX_MESSAGE_LENGTH) {
            firstLine = firstLine.substring(0, MAX_MESSAGE_LENGTH);
        }

        return wasTruncated ? firstLine + TRUNCATED_SUFFIX : firstLine;
    }

    private record TelegramMessage(String chat_id, String text) {}
}
