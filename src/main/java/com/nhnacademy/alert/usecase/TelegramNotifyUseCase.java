package com.nhnacademy.alert.usecase;

import com.nhnacademy.alert.common.annotation.UseCase;
import com.nhnacademy.alert.dto.FiringAlertCommand;
import com.nhnacademy.alert.service.TelegramService;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class TelegramNotifyUseCase {
    private final TelegramService telegramService;

    public void execute(FiringAlertCommand command) {
        telegramService.sendError(command);
    }
}
