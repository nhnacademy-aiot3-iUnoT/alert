package com.nhnacademy.alert.usecase;

import com.nhnacademy.alert.common.UseCase;
import com.nhnacademy.alert.dto.FiringAlertCommand;
import com.nhnacademy.alert.service.LogSearchService;
import com.nhnacademy.alert.service.TelegramService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@UseCase
@RequiredArgsConstructor
public class TelegramNotifyUseCase {
    private final TelegramService telegramService;
    private final LogSearchService logSearchService;

    public void execute(List<FiringAlertCommand> commands) {
        commands.forEach(c -> logSearchService.searchLatestError(c.containerName())
                .ifPresent(l -> telegramService.sendSingleError(c.containerName(), l)));
    }
}
