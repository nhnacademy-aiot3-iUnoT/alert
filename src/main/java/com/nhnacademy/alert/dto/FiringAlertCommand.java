package com.nhnacademy.alert.dto;

import java.time.Instant;

public record FiringAlertCommand(
        String containerName,
        Instant startsAt
) {}
