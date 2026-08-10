package com.nhnacademy.alert.usecase;

import com.nhnacademy.alert.dto.ErrorLogCommand;
import com.nhnacademy.alert.dto.LogEntry;
import com.nhnacademy.alert.service.AlertRateLimiter;
import com.nhnacademy.alert.service.LogRefineService;
import com.nhnacademy.alert.service.LogSummaryService;
import com.nhnacademy.alert.service.TelegramService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TelegramNotifyUseCaseTest {

    @Mock
    private LogRefineService logRefineService;

    @Mock
    private LogSummaryService logSummaryService;

    @Mock
    private TelegramService telegramService;

    @Mock
    private AlertRateLimiter alertRateLimiter;

    @InjectMocks
    private TelegramNotifyUseCase telegramNotifyUseCase;

    @Test
    @DisplayName("로그 정제 서비스가 null을 반환하면 알림을 보내지 않는다.")
    void execute_WhenRefineLogReturnsNull_ShouldNotProceed() {
        // given
        ErrorLogCommand command = new ErrorLogCommand(Instant.now(), "ERROR", "Test message", "trace123", "span456", "test-container");
        given(logRefineService.refineLog(command))
                .willReturn(null);

        // when
        telegramNotifyUseCase.execute(command);

        // then
        then(logSummaryService)
                .should(never())
                .summarize(any());
        then(telegramService)
                .should(never())
                .sendError(any(), any(), any());
        then(alertRateLimiter)
                .should(never())
                .shouldNotify(anyString(), anyString());
    }

    @Test
    @DisplayName("알림 제한 서비스가 알림을 보내지 않도록 지시하면 텔레그램 알림을 보내지 않는다.")
    void execute_WhenShouldNotifyReturnsFalse_ShouldNotSendTelegram() {
        // given
        ErrorLogCommand command = new ErrorLogCommand(Instant.now(), "ERROR", "Test message", "trace123", "span456", "test-container");
        LogEntry logEntry = new LogEntry("2023-01-01 10:00:00", "test-container", "Test message", "stacktrace");

        given(logRefineService.refineLog(command))
                .willReturn(logEntry);
        given(alertRateLimiter.shouldNotify(logEntry.containerName(), logEntry.message()))
                .willReturn(false);

        // when
        telegramNotifyUseCase.execute(command);

        // then
        then(logSummaryService)
                .should(never())
                .summarize(any());
        then(telegramService)
                .should(never())
                .sendError(any(), any(), any());
    }

    @Test
    @DisplayName("알림 제한 서비스가 알림을 보내도록 지시하면 텔레그램 알림을 보낸다.")
    void execute_WhenShouldNotifyReturnsTrue_ShouldSendTelegram() {
        // given
        Instant now = Instant.now();
        ErrorLogCommand command = new ErrorLogCommand(now, "ERROR", "Test message", "trace123", "span456", "test-container");
        LogEntry logEntry = new LogEntry("2023-01-01 10:00:00", "test-container", "Test message", "stacktrace");
        String summary = "AI summary of the log";

        given(logRefineService.refineLog(command))
                .willReturn(logEntry);
        given(alertRateLimiter.shouldNotify(logEntry.containerName(), logEntry.message()))
                .willReturn(true);
        given(logSummaryService.summarize(logEntry))
                .willReturn(summary);

        // when
        telegramNotifyUseCase.execute(command);

        // then
        then(logSummaryService)
                .should()
                .summarize(logEntry);
        then(telegramService)
                .should()
                .sendError(logEntry, summary, command.traceId());
    }
}
