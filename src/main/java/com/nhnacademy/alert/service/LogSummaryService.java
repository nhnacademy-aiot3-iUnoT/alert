package com.nhnacademy.alert.service;

import com.nhnacademy.alert.dto.LogEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogSummaryService {
    private final ChatClient.Builder chatClientBuilder;
    private static final String PROMPT = """
       당신은 백엔드 개발팀에게 에러 알림을 전달하는 역할을 수행합니다. 주어진 로그를 가지고 다음 두 가지만 답하세요.
       1) 원인: 한 문장
       2) 확인할 것: 한 문장
       설명을 너무 길게 하지 않고, 이미 개발자가 아는 내용은 반복하지 마세요. 강조 문법을 사용하지 않고 단순히 텍스트로만 응답합니다.
       """;

    public String summarize(LogEntry logEntry) {
        // 로그가 없거나 스택 트레이스가 없는 경우 AI 분석을 하지 않음
        if (logEntry == null || logEntry.stackTrace() == null || logEntry.stackTrace().isBlank()) {
            return "";
        }

        try {
            return chatClientBuilder.build()
                    .prompt()
                    .system(PROMPT)
                    .user(String.valueOf(logEntry))
                    .call()
                    .content();
        } catch (Exception e) {
            log.warn("AI 요약 실패", e);
            return "";
        }
    }
}
