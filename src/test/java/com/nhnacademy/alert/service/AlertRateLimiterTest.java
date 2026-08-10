package com.nhnacademy.alert.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertRateLimiterTest {

    private final AlertRateLimiter alertRateLimiter = new AlertRateLimiter();

    @Test
    @DisplayName("처음 처리하는 로그면 true를 반환한다.")
    void shouldNotify_WhenFirstTimeProcessing_ReturnsTrue() {
        // given
        String containerName = "account";
        String message = "외부 API 호출 실패";

        // when
        boolean result = alertRateLimiter.shouldNotify(containerName, message);

        // then
        assertThat(result)
                .isTrue();
    }

    @Test
    @DisplayName("같은 fingerprint로 온 로그는 false를 반환한다.")
    void shouldNotify_WhenSameFingerprint_ReturnsFalse() {
        // given
        String containerName = "account";
        String message = "외부 API 호출 실패";

        alertRateLimiter.shouldNotify(containerName, message);

        // when
        boolean result = alertRateLimiter.shouldNotify(containerName, message);

        // then
        assertThat(result)
                .isFalse();
    }

    @Test
    @DisplayName("동일한 컨테이너라도 메시지가 다르면 모두 true를 반환한다.")
    void shouldNotify_WhenSameContainerAndDifferentMessage_ReturnsTrue() {
        // given
        String containerName = "account";
        String message1 = "외부 API 호출 실패";
        String message2 = "서버 내부 오류 발생";

        // when
        boolean result1 = alertRateLimiter.shouldNotify(containerName, message1);
        boolean result2 = alertRateLimiter.shouldNotify(containerName, message2);

        // then
        assertThat(result1)
                .isTrue();
        assertThat(result2)
                .isTrue();
    }
}