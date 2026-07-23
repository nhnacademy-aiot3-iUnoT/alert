package com.nhnacademy.alert.dto;

import java.time.Instant;

public record LogEntry(
        Instant timestamp,
        String containerName,
        String logger,
        String message
) {}
