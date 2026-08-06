package com.nhnacademy.alert.usecase;

import com.nhnacademy.alert.common.annotation.UseCase;
import com.nhnacademy.alert.dto.ErrorLogCommand;
import com.nhnacademy.alert.dto.LogEntry;
import com.nhnacademy.alert.service.AlertRateLimiter;
import com.nhnacademy.alert.service.LogRefineService;
import com.nhnacademy.alert.service.LogSummaryService;
import com.nhnacademy.alert.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class TelegramNotifyUseCase {
    private final LogRefineService logRefineService;
    private final LogSummaryService logSummaryService;
    private final TelegramService telegramService;
    private final AlertRateLimiter alertRateLimiter;

    public void execute(ErrorLogCommand command) {
        LogEntry logEntry = logRefineService.refineLog(command);

        if (logEntry == null) {
            return;
        }

        log.info("정제된 로그: {}", logEntry);

        if (alertRateLimiter.shouldNotify(logEntry.containerName(), logEntry.message())) {
            String summary = logSummaryService.summarize(logEntry);

            log.info("로그 AI 요약: {}", summary);

            telegramService.sendError(logEntry, summary, command.traceId());
        }
    }
}
