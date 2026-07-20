package com.nhnacademy.alert.dto;

public record LogEntry(
        String timestamp,
        String containerName,
        String logger,
        String message
) {}
