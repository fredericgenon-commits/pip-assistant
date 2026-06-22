package com.utmost.lu.pipassistant.infrastructure.jira;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.utmost.lu.pipassistant.domain.port.JiraPort;

/** Deterministic stub for local development without JIRA (activate with profile jira-mock). */
@Component
@Profile("jira-mock")
public class MockJiraAdapter implements JiraPort {

    private static final List<String> STATUSES = List.of("To Do", "In Progress", "In Review", "Done");

    @Override
    public Optional<String> fetchStatus(String issueKey) {
        return Optional.of(STATUSES.get(Math.abs(issueKey.hashCode()) % STATUSES.size()));
    }
}
