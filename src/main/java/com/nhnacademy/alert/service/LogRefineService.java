package com.nhnacademy.alert.service;

import com.nhnacademy.alert.dto.ErrorLogCommand;
import com.nhnacademy.alert.dto.LogEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogRefineService {
    private final DateTimeFormatter formatter;

    public LogEntry refineLog(ErrorLogCommand command) {
        String logMessage = command.logMessage();

        if (logMessage == null || logMessage.isEmpty()) {
            return null;
        }

        int newlineIndex = logMessage.indexOf('\n');
        boolean hasStackTrace = newlineIndex != -1;

        String message = (hasStackTrace) ? logMessage.substring(0, newlineIndex) : logMessage;
        String fullStackTrace = (hasStackTrace) ? logMessage.substring(newlineIndex + 1).stripLeading() : "";
        String refinedStackTrace = extractRelevantStackTrace(fullStackTrace);
        String formattedTime = formatter.format(command.logTimestamp());

        return new LogEntry(formattedTime, command.containerName(), message, refinedStackTrace);
    }

    private String extractRelevantStackTrace(String fullStackTrace) {
        List<String> lines = List.of(fullStackTrace.split("\n"));

        if (lines.isEmpty()) {
            return "";
        }

        String exceptionLine = lines.getFirst();
        String relevantFrames = lines.stream()
                .skip(1)
                .filter(line -> line.contains("com.nhnacademy"))
                .limit(30)
                .collect(Collectors.joining("\n"));

        return relevantFrames.isEmpty() ? exceptionLine : exceptionLine + "\n" + relevantFrames;
    }
}
