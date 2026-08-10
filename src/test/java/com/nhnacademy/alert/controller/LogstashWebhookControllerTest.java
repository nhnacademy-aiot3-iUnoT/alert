package com.nhnacademy.alert.controller;

import com.nhnacademy.alert.dto.ErrorLogCommand;
import com.nhnacademy.alert.dto.ErrorLogRequest;
import com.nhnacademy.alert.usecase.TelegramNotifyUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogstashWebhookController.class)
class LogstashWebhookControllerTest {

    @MockitoBean
    private TelegramNotifyUseCase telegramNotifyUseCase;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("유효한 Logstash 페이로드로 웹훅을 호출하면 텔레그램 알림 유스케이스가 실행된다.")
    void handleErrorLog_WhenValidRequest_CallsTelegramNotifyUseCase() throws Exception {
        // given
        Instant now = Instant.now();
        ErrorLogRequest.Container container = new ErrorLogRequest.Container("test-container");
        ErrorLogRequest request = new ErrorLogRequest(now, "ERROR", "Test message", "trace123", "span456", container);
        ErrorLogCommand expected = new ErrorLogCommand(now, "ERROR", "Test message", "trace123", "span456", "test-container");

        // when
        ResultActions result = mockMvc.perform(post("/webhook/logstash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk());

        then(telegramNotifyUseCase)
                .should()
                .execute(expected);
    }
}
