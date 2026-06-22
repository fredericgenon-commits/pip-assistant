package com.utmost.lu.pipassistant.infrastructure.jira;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.utmost.lu.pipassistant.domain.model.DevTicket;
import com.utmost.lu.pipassistant.domain.port.JiraBacklogPort;
import com.utmost.lu.pipassistant.domain.port.JiraPort;
import com.utmost.lu.pipassistant.infrastructure.config.JiraProperties;

/**
 * Real JIRA adapter for production use (profile {@code !jira-mock}).
 * Authenticates via Bearer token (PAT) and calls the JIRA Server REST API v2.
 */
@Component
@Profile("!jira-mock")
public class JiraClientAdapter implements JiraPort, JiraBacklogPort {

    private final RestClient restClient;
    private final JiraProperties props;

    public JiraClientAdapter(JiraProperties props) {
        this.props = props;
        this.restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + props.getApiToken())
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    public Optional<String> fetchStatus(String issueKey) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                    .uri("/rest/api/2/issue/{key}?fields=status", issueKey)
                    .retrieve()
                    .body(Map.class);
            if (body == null) {
                return Optional.empty();
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> fields = (Map<String, Object>) body.get("fields");
            @SuppressWarnings("unchecked")
            Map<String, Object> status = fields != null ? (Map<String, Object>) fields.get("status") : null;
            Object name = status != null ? status.get("name") : null;
            return Optional.ofNullable(name != null ? name.toString() : null);
        } catch (RestClientException e) {
            throw new JiraCallException("fetchStatus(" + issueKey + ")", e);
        }
    }

    @Override
    public List<DevTicket> fetchDevTickets(String reqKey) {
        JiraProperties.Fields f = props.getField();
        String fields = String.join(",", "key", "summary", "status",
                f.getDeliveryMethod(), f.getTeam(), f.getStoryPoints());
        String jql = "issueType=Story AND \"Epic Link\"=" + reqKey;

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/rest/api/2/search")
                            .queryParam("jql", jql)
                            .queryParam("fields", fields)
                            .queryParam("maxResults", "100")
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (body == null) {
                return List.of();
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> issues = (List<Map<String, Object>>) body.get("issues");
            if (issues == null) {
                return List.of();
            }

            List<DevTicket> tickets = new ArrayList<>();
            for (Map<String, Object> issue : issues) {
                String key = (String) issue.get("key");
                @SuppressWarnings("unchecked")
                Map<String, Object> ticketFields = (Map<String, Object>) issue.get("fields");
                if (ticketFields == null) {
                    continue;
                }
                String summary = (String) ticketFields.get("summary");
                @SuppressWarnings("unchecked")
                Map<String, Object> statusObj = (Map<String, Object>) ticketFields.get("status");
                String status = statusObj != null ? (String) statusObj.get("name") : null;

                String deliveryMethod = extractSelectValue(ticketFields, f.getDeliveryMethod());
                String team = extractSelectValue(ticketFields, f.getTeam());
                Integer sp = extractInt(ticketFields, f.getStoryPoints());

                tickets.add(new DevTicket(key, summary, status, deliveryMethod, team, sp));
            }
            return tickets;
        } catch (RestClientException e) {
            throw new JiraCallException("fetchDevTickets(" + reqKey + ")", e);
        }
    }

    /** Extracts the "value" from a JIRA select/option field (returns null if absent). */
    @SuppressWarnings("unchecked")
    private static String extractSelectValue(Map<String, Object> fields, String fieldId) {
        Object raw = fields.get(fieldId);
        if (raw instanceof Map<?, ?> map) {
            Object value = map.get("value");
            return value != null ? value.toString() : null;
        }
        return raw != null ? raw.toString() : null;
    }

    private static Integer extractInt(Map<String, Object> fields, String fieldId) {
        Object raw = fields.get(fieldId);
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(raw.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
