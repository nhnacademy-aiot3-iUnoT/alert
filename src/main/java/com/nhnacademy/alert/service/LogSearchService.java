package com.nhnacademy.alert.service;

import tools.jackson.databind.JsonNode;
import com.nhnacademy.alert.dto.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LogSearchService {

    private static final int MAX_HITS = 10;

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

    public List<LogEntry> searchErrors(String containerName, Instant from, Instant to) {
        try {
            JsonNode response = restClient.post()
                    .uri("/{index}/_search", indexPattern)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildQuery(containerName, from, to))
                    .retrieve()
                    .body(JsonNode.class);

            return parseHits(response);
        } catch (Exception e) {
            log.error("Elasticsearch 로그 조회 실패", e);
            return List.of();
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

    private List<LogEntry> parseHits(JsonNode response) {
        List<LogEntry> entries = new ArrayList<>();
        if (response == null) {
            return entries;
        }

        for (JsonNode hit : response.path("hits").path("hits")) {
            JsonNode source = hit.path("_source");
            entries.add(new LogEntry(
                    source.path("@timestamp").asString(""),
                    source.path("container").path("name").asString("unknown"),
                    source.path("log").path("logger").asString(""),
                    source.path("message").asString("")
            ));
        }
        return entries;
    }
}
