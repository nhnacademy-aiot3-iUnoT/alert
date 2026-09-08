package com.nhnacademy.alert.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.regex.Pattern;

@Service
public class AlertRateLimiter {

    private static final int MAX_ENTRIES = 10_000;

    // ISO 타임스탬프, 게이트웨이 requestId, UUID, 경로의 식별자(/storages/2)를 정규화해서 중복 억제
    private static final String VOLATILE_PATTERN = """
        \\d{4}-\\d{2}-\\d{2}[T\\s]\\d{2}:\\d{2}:\\d{2}[.,]?\\d*Z?
        |(?<="requestId":")[^"]*
        |[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}
        |(?<=/)\\d+(?=[/"?\\s]|$)
        """;

    private static final Pattern VOLATILE = Pattern.compile(VOLATILE_PATTERN, Pattern.COMMENTS);

    private final Cache<String, Boolean> recentAlerts = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(60))
            .maximumSize(MAX_ENTRIES)
            .build();

    public boolean shouldNotify(String containerName, String message) {
        String fingerprint = containerName + "|" + normalize(message);

        // 키가 이미 존재: 기존에 저장되어 있던 값을 반환
        // 키가 미존재: 새 값을 저장하고 null을 반환
        return recentAlerts.asMap().putIfAbsent(fingerprint, true) == null;
    }

    private String normalize(String message) {
        return (message == null) ? "" : VOLATILE.matcher(message).replaceAll("#");
    }
}
