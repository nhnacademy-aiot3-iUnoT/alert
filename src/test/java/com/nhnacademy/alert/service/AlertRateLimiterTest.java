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

    @Test
    @DisplayName("타임스탬프와 식별자만 다른 로그는 같은 장애로 보고 false를 반환한다.")
    void shouldNotify_WhenOnlyVolatileValuesDiffer_ReturnsFalse() {
        // given
        String containerName = "team1-front-2";
        String first = "게이트웨이 응답을 해석할 수 없음: status=503, body={\"timestamp\":\"2026-09-07T09:07:11.697Z\","
                + "\"path\":\"/api/core/assistant/notes/9/read\",\"requestId\":\"045c28cd-59723\"}";
        String second = "게이트웨이 응답을 해석할 수 없음: status=503, body={\"timestamp\":\"2026-09-07T09:31:02.114Z\","
                + "\"path\":\"/api/core/assistant/notes/17/read\",\"requestId\":\"9f31ab04-88210\"}";

        alertRateLimiter.shouldNotify(containerName, first);

        // when
        boolean result = alertRateLimiter.shouldNotify(containerName, second);

        // then
        assertThat(result)
                .isFalse();
    }

    @Test
    @DisplayName("상태 코드가 다르면 다른 장애로 보고 true를 반환한다.")
    void shouldNotify_WhenStatusCodeDiffers_ReturnsTrue() {
        // given
        String containerName = "team1-front-2";

        alertRateLimiter.shouldNotify(containerName, "게이트웨이 요청 실패: status=503");

        // when
        boolean result = alertRateLimiter.shouldNotify(containerName, "게이트웨이 요청 실패: status=500");

        // then
        assertThat(result)
                .isTrue();
    }

    @Test
    @DisplayName("requestId 의 자릿수가 달라도 같은 장애로 보고 false를 반환한다.")
    void shouldNotify_WhenRequestIdLengthDiffers_ReturnsFalse() {
        // given
        String containerName = "team1-front-1";
        String first = "게이트웨이 통신 에러: status=503, body={\"timestamp\":\"2026-09-08T02:04:26.144Z\","
                + "\"path\":\"/api/core/assistant/notes\",\"requestId\":\"c2b72284-711\"}";
        String second = "게이트웨이 통신 에러: status=503, body={\"timestamp\":\"2026-09-08T02:04:25.241Z\","
                + "\"path\":\"/api/core/assistant/notes\",\"requestId\":\"76c77b89-710\"}";

        alertRateLimiter.shouldNotify(containerName, first);

        // when
        boolean result = alertRateLimiter.shouldNotify(containerName, second);

        // then
        assertThat(result)
                .isFalse();
    }
}
