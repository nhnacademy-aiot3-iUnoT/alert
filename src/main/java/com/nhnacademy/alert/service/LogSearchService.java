package com.nhnacademy.alert.service;

import tools.jackson.databind.JsonNode;
import com.nhnacademy.alert.dto.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
public class LogSearchService {

    private static final int MAX_HITS = 1;

    private final RestClient restClient;
    private final String indexPattern;

    public LogSearchService(
            @Value("${es.host}") String esHost,
            @Value("${es.user}") String esUser,
            @Value("${es.password}") String esPassword,
            @Value("${es.index-pattern}") String indexPattern
    ) {
        this.indexPattern = indexPattern;
        this.restClient = RestClient.builder()
                .baseUrl(esHost)
                .defaultHeaders(headers -> headers.setBasicAuth(esUser, esPassword))
                .build();
    }

    public Optional<LogEntry> searchLatestError(String containerName) {
        Instant to = Instant.now();
        Instant from = to.minus(Duration.ofMinutes(1));

        try {
            JsonNode response = restClient.post()
                    .uri("/{index}/_search", indexPattern)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildQuery(containerName, from, to))
                    .retrieve()
                    .body(JsonNode.class);

            log.info("ES 에러 로그: {}", response);

            return parseHits(response);
        } catch (Exception e) {
            log.warn("ES 조회 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String buildQuery(String containerName, Instant from, Instant to) {
        String containerFilter = containerName == null || containerName.isBlank()
                ? ""
                : ",{\"term\":{\"container.name\":\"%s\"}}".formatted(containerName);

        return """
                {
                  "size": %d,
                  "sort": [{"@timestamp": "desc"}],
                  "query": {
                    "bool": {
                      "filter": [
                        {"match": {"log.level": "ERROR"}},
                        {"range": {"@timestamp": {"gte": "%s", "lte": "%s"}}}
                        %s
                      ]
                    }
                  }
                }
                """.formatted(MAX_HITS, from, to, containerFilter);
    }

    private Optional<LogEntry> parseHits(JsonNode response) {
        if (response == null) {
            return Optional.empty();
        }

        JsonNode hit = response.path("hits").path("hits");

        if (hit.isEmpty()) {
            return Optional.empty();
        }

        JsonNode source = hit.get(0).path("_source");

        LogEntry error = new LogEntry(
                source.path("@timestamp").asString(""),
                source.path("container").path("name").asString("unknown"),
                source.path("log").path("logger").asString(""),
                source.path("message").asString("")
        );

        return Optional.of(error);
    }
}
