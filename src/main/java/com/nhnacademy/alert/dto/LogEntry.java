package com.nhnacademy.alert.dto;

public record LogEntry(
        String timestamp,
        String containerName,
        String message,
        String stackTrace
) {}
