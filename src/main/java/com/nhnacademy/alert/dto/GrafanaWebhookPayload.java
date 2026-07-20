package com.nhnacademy.alert.dto;

import java.util.List;
import java.util.Map;

public record GrafanaWebhookPayload(
        String receiver,
        String status,
        List<Alert> alerts
) {
    public record Alert(
            String status,
            Map<String, String> labels,
            Map<String, String> annotations,
            String startsAt,
            String endsAt
    ) {}
}
