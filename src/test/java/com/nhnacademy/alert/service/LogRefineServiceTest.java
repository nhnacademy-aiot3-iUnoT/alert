package com.nhnacademy.alert.service;

import com.nhnacademy.alert.dto.ErrorLogCommand;
import com.nhnacademy.alert.dto.LogEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LogRefineServiceTest {

    @Mock
    private DateTimeFormatter formatter;

    @InjectMocks
    private LogRefineService logRefineService;

    @Test
    @DisplayName("로그 메시지가 존재하지 않거나 비어 있으면 null이 반환된다.")
    void refineLog_WhenLogMessageNull_ReturnsNull() {
        // given
        ErrorLogCommand command1 = new ErrorLogCommand(Instant.now(), "INFO", null, "", "", "account");
        ErrorLogCommand command2 = new ErrorLogCommand(Instant.now(), "INFO", "", "", "", "account");

        // when
        LogEntry result1 = logRefineService.refineLog(command1);
        LogEntry result2 = logRefineService.refineLog(command2);

        // then
        assertThat(result1)
                .isNull();
        assertThat(result2)
                .isNull();
    }

    @Test
    @DisplayName("스태트레이스가 있으면 예외 첫 줄과 com.nhnacademy 프레임만 추출한다.")
    void refineLog_WhenStackTraceExists_ExtractsRelevantFrames() {
        // given
        String logMessage = """
            커넥션 풀 고갈로 요청 처리 실패
            java.lang.RuntimeException: pool exhausted
                    \\tat com.nhnacademy.inventory.service.StorageService.find(StorageService.java:42)
                    \\tat org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963)
            """;

        ErrorLogCommand command = new ErrorLogCommand(Instant.now(), "INFO", logMessage, "", "", "account");

        // when
        LogEntry result = logRefineService.refineLog(command);

        // then
        assertThat(result.message())
                .isEqualTo("커넥션 풀 고갈로 요청 처리 실패");
        assertThat(result.stackTrace())
                .contains("java.lang.RuntimeException: pool exhausted")
                .contains("com.nhnacademy")
                .doesNotContain("org.springframework");
    }

    @Test
    @DisplayName("스택트레이스가 없으면 message만 채워지고 stackTrace는 빈 문자열이다.")
    void refineLog_WhenStackTraceNotExists_EmptyStackTrace() {
        // given
        ErrorLogCommand command = new ErrorLogCommand(Instant.now(), "INFO", "외부 API 응답 타임아웃", "", "", "account");

        // when
        LogEntry result = logRefineService.refineLog(command);

        // then
        assertThat(result.message())
                .isEqualTo("외부 API 응답 타임아웃");
        assertThat(result.stackTrace())
                .isBlank();
    }
}