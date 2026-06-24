package com.utmost.lu.pipassistant.domain.port;

import java.util.Optional;

/** Outbound port for fetching a single JIRA issue status. */
public interface JiraPort {

    /**
     * Fetches the current status of a JIRA issue by its key.
     *
     * @param issueKey the JIRA issue key (e.g. "REQ-511")
     * @return the status name, or empty when the issue cannot be found
     */
    Optional<String> fetchStatus(String issueKey);
}
