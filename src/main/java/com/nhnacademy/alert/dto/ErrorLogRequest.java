package com.nhnacademy.alert.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record ErrorLogRequest(
        @JsonProperty("log_timestamp")
        Instant logTimestamp,

        @JsonProperty("log_level")
        String logLevel,

        @JsonProperty("log_message")
        String logMessage,

        @JsonProperty("trace_id")
        String traceId,

        @JsonProperty("span_id")
        String spanId,

        Container container
) {
    public record Container(String name) {}

    public FiringAlertCommand toCommand() {
        return new FiringAlertCommand(logTimestamp, logLevel, logMessage, traceId, spanId, container.name());
    }
}
