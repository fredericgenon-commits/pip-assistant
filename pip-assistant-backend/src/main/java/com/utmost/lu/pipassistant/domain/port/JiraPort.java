package com.utmost.lu.pipassistant.domain.port;

import java.util.Optional;

/** Outbound port for fetching ticket data from JIRA. */
public interface JiraPort {

    /** Returns the current status name of the given JIRA issue, or empty if not found. */
    Optional<String> fetchStatus(String issueKey);
}
