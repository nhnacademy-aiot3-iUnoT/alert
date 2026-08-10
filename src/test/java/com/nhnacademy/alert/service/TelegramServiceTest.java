package com.nhnacademy.alert.service;

import com.nhnacademy.alert.common.config.TelegramProperties;
import com.nhnacademy.alert.common.config.ZipkinProperties;
import com.nhnacademy.alert.dto.LogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TelegramServiceTest {

    @Mock
    private RestClient telegramRestClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private TelegramService telegramService;

    @BeforeEach
    void setUp() {
        TelegramProperties telegramProperties = new TelegramProperties("bot-token", "12345");
        ZipkinProperties zipkinProperties = new ZipkinProperties("https://zipkin.iunot.cloud");

        telegramService = new TelegramService(telegramProperties, zipkinProperties, telegramRestClient);
    }

    @Test
    @DisplayName("정상적인 로그면 텔레그램으로 HTML 메시지를 전송한다.")
    void sendError_WhenValidLog_SendsHtmlMessage() {
        // given
        LogEntry logEntry = new LogEntry(
                "2026-01-01 10:00:00",
                "account",
                "테스트 <메시지>",
                "java.lang.RuntimeException: 테스트"
        );

        given(telegramRestClient.post())
                .willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri("/sendMessage"))
                .willReturn(requestBodySpec);
        given(requestBodySpec.contentType(MediaType.APPLICATION_JSON))
                .willReturn(requestBodySpec);
        given(requestBodySpec.body(any()))
                .willReturn(requestBodySpec);
        given(requestBodySpec.retrieve())
                .willReturn(responseSpec);
        given(responseSpec.toBodilessEntity())
                .willReturn(null);

        // when
        telegramService.sendError(logEntry, "원인: 테스트입니다.", "trace-id-123");

        // then
        ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        then(requestBodySpec)
                .should()
                .body(bodyCaptor.capture());

        String message = bodyCaptor.getValue().toString();
        assertThat(message)
                .contains("account")
                .contains("&lt;메시지&gt;") // HTML 이스케이프 확인
                .contains("원인: 테스트입니다.")
                .contains("https://zipkin.iunot.cloud/zipkin/traces/trace-id-123");
    }

    @Test
    @DisplayName("전송 중 예외가 발생해도 밖으로 전파되지 않는다.")
    void sendError_WhenRestClientThrows_DoesNotPropagate() {
        // given
        LogEntry logEntry = new LogEntry("2026-01-01 10:00:00", "account", "테스트", "java.lang.RuntimeException: 테스트");

        given(telegramRestClient.post())
                .willThrow(new RuntimeException("연결 실패"));

        // when & then
        assertDoesNotThrow(() -> telegramService.sendError(logEntry, "", null));
    }
}