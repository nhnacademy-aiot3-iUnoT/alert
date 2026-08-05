package com.nhnacademy.alert.usecase;

import com.nhnacademy.alert.common.annotation.UseCase;
import com.nhnacademy.alert.dto.ErrorLogCommand;
import com.nhnacademy.alert.dto.LogEntry;
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

    public void execute(ErrorLogCommand command) {
        LogEntry logEntry = logRefineService.refineLog(command);

        log.info("정제된 로그: {}", logEntry);

        String summary = logSummaryService.summarize(logEntry);

        log.info("요약된 로그: {}", summary);

        telegramService.sendError(logEntry, summary);
    }
}
