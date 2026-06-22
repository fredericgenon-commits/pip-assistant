package com.utmost.lu.pipassistant.infrastructure.jira;

/** Wraps a JIRA HTTP call failure with context about which call failed. */
public class JiraCallException extends RuntimeException {

    public JiraCallException(String context, Throwable cause) {
        super("JIRA call failed: " + context, cause);
    }
}
