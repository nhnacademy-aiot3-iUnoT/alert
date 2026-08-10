package com.nhnacademy.alert.service;

import com.nhnacademy.alert.dto.LogEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class LogSummaryServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @InjectMocks
    private LogSummaryService logSummaryService;

    @Test
    @DisplayName("로그가 null인 경우 AI 요약을 하지 않고 빈 문자열을 반환한다.")
    void summarize_WhenNullLog_ReturnsEmptyString() {
        // when
        String result = logSummaryService.summarize(null);

        // then
        assertThat(result)
                .isEmpty();

        then(chatClientBuilder)
                .should(never())
                .build();
    }

    @Test
    @DisplayName("스택트레이스가 null이거나 비어있는 경우 AI 요약을 하지 않고 빈 문자열을 반환한다.")
    void summarize_WhenEmptyStackTrace_ReturnsEmptyString() {
        // given
        LogEntry logEntry1 = new LogEntry("2026-01-01", "account", "테스트 로그", "");
        LogEntry logEntry2 = new LogEntry("2026-01-01", "account", "테스트 로그", null);

        // when
        String result1 = logSummaryService.summarize(logEntry1);
        String result2 = logSummaryService.summarize(logEntry2);

        // then
        assertThat(result1)
                .isEmpty();
        assertThat(result2)
                .isEmpty();

        then(chatClientBuilder)
                .should(never())
                .build();
    }

    @Test
    @DisplayName("스택트레이스가 있으면 AI 요약을 호출하고 결과를 반환한다.")
    void summarize_WhenStackTraceExists_ReturnsAiSummary() {
        // given
        LogEntry logEntry = new LogEntry(
                "2026-01-01",
                "account",
                "테스트 로그",
                "java.lang.RuntimeException: 테스트 예외\n\tat com.nhnacademy.account.Test.run(Test.java:1)"
        );

        given(chatClientBuilder.build())
                .willReturn(chatClient);
        given(chatClient.prompt())
                .willReturn(requestSpec);
        given(requestSpec.system(anyString()))
                .willReturn(requestSpec);
        given(requestSpec.user(anyString()))
                .willReturn(requestSpec);
        given(requestSpec.call())
                .willReturn(callResponseSpec);
        given(callResponseSpec.content())
                .willReturn("원인: 테스트 예외입니다.\n확인할 것: 코드를 확인하세요.");

        // when
        String result = logSummaryService.summarize(logEntry);

        // then
        assertThat(result)
                .isEqualTo("원인: 테스트 예외입니다.\n확인할 것: 코드를 확인하세요.");
    }
}