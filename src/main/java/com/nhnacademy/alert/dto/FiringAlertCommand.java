package com.nhnacademy.alert.dto;

import java.time.Instant;

public record FiringAlertCommand(
        Instant logTimestamp,
        String logLevel,
        String logMessage,
        String traceId,
        String spanId,
        String containerName
) {}
