package com.nhnacademy.alert.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AlertRateLimiter {
    private final Cache<String, Boolean> recentAlerts = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .build();

    public boolean shouldNotify(String containerName, String message) {
        String fingerprint = containerName + "|" + message;

        // 키가 이미 존재: 기존에 저장되어 있던 값을 반환
        // 기가 미존재: 새 값을 저장하고 null을 반환
        return recentAlerts.asMap().putIfAbsent(fingerprint, true) == null;
    }
}
