package com.nhnacademy.alert.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record GrafanaWebhookRequest(
        String status,
        List<Alert> alerts
) {
    public record Alert(
            String status,
            Map<String, String> labels,
            String startsAt
    ) {}

    public List<FiringAlertCommand> toCommand() {
        if (status == null || !Objects.equals("firing", status)) {
            return List.of();
        }

        return alerts.stream()
                .filter(a -> a.startsAt != null)
                .filter(a -> a.labels.containsKey("container.name"))
                .map(a -> new FiringAlertCommand(a.labels.get("container.name"), Instant.parse(a.startsAt)))
                .toList();
    }
}

/*
{
  "receiver": "WebHook",
  "status": "firing",
  "alerts": [
    {
      "status": "firing",
      "labels": {
        "alertname": "ERROR 로그 알림",
        "container.name": "team1-alert",
        "grafana_folder": "Alerts"
      },
      "annotations": {},
      "startsAt": "2026-07-23T05:20:30Z",
      "endsAt": "0001-01-01T00:00:00Z",
      "generatorURL": "http://localhost:3000/alerting/grafana/afsnpdje7rnr4d/view?orgId=1",
      "fingerprint": "8b1971c962be25d4",
      "silenceURL": "http://localhost:3000/alerting/silence/new?alertmanager=grafana&matcher=__alert_rule_uid__%3Dafsnpdje7rnr4d&matcher=container.name%3Dteam1-alert&orgId=1",
      "dashboardURL": "",
      "panelURL": "",
      "ruleUID": "afsnpdje7rnr4d",
      "values": {
        "A": 3,
        "B": 3,
        "C": 1
      },
      "valueString": "[ var='A' labels={container.name=team1-alert} type='query' value=3 ], [ var='B' labels={container.name=team1-alert} type='reduce' value=3 ], [ var='C' labels={container.name=team1-alert} type='threshold' value=1 ]",
      "orgId": 1
    }
  ]
}
 */