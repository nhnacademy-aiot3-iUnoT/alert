package com.nhnacademy.alert.service;

import com.nhnacademy.alert.common.config.TelegramProperties;
import com.nhnacademy.alert.common.config.ZipkinProperties;
import com.nhnacademy.alert.dto.LogEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {
    private final TelegramProperties telegramProperties;
    private final ZipkinProperties zipkinProperties;
    private final RestClient telegramRestClient;

    public void sendError(LogEntry logEntry, String summary, String traceId) {
        try {
            telegramRestClient.post()
                    .uri("/sendMessage")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(formatMessage(logEntry, summary, traceId))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("텔레그램 알림 전송 실패", e);
        }
    }

    private TelegramMessage formatMessage(LogEntry logEntry, String summary, String traceId) {
        String template = """
        <b>[ERROR] %s</b>
        
        %s
        
        %s
        %s
        %s
        <pre><code class=language-java>
        %s
        </code></pre>
        """;

        String message = String.format(template,
                escapeHtml(logEntry.containerName()),
                logEntry.timestamp(),
                escapeHtml(logEntry.message()),
                formatAiSection(summary),
                formatTraceLink(traceId),
                escapeHtml(logEntry.stackTrace()));

        return new TelegramMessage(telegramProperties.chatId(), message, "HTML");
    }

    private String formatAiSection(String summary) {
        return (summary == null || summary.isBlank()) ? "" : """
        
        <b>[AI 분석]</b>
        %s
        """.formatted(escapeHtml(summary));
    }

    private String formatTraceLink(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            return "";
        }

        return "%n<a href=\"%s/zipkin/traces/%s\">Zipkin에서 보기</a>%n"
                .formatted(zipkinProperties.baseUrl(), traceId);
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private record TelegramMessage(String chat_id, String text, String parse_mode) {}
}