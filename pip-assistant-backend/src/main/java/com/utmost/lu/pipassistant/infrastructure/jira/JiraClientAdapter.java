package com.utmost.lu.pipassistant.infrastructure.jira;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.utmost.lu.pipassistant.domain.port.JiraPort;
import com.utmost.lu.pipassistant.infrastructure.config.JiraProperties;

/** Calls JIRA Server / Data Center REST API v2 using a Personal Access Token (Bearer). */
@Component
@Profile("!jira-mock")
public class JiraClientAdapter implements JiraPort {

    private final RestClient restClient;

    public JiraClientAdapter(JiraProperties props) {
        this.restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + props.getApiToken())
                .build();
    }

    @Override
    public Optional<String> fetchStatus(String issueKey) {
        try {
            JiraIssueResponse response = restClient.get()
                    .uri("/rest/api/2/issue/{key}?fields=status", issueKey)
                    .retrieve()
                    .body(JiraIssueResponse.class);
            if (response == null || response.fields() == null || response.fields().status() == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.fields().status().name());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    private record JiraIssueResponse(Fields fields) {
        private record Fields(StatusRef status) {}
        private record StatusRef(String name) {}
    }
}
