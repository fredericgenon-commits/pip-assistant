package com.utmost.lu.pipassistant.infrastructure.web.dto;

/** Frontend-facing JIRA sync configuration values. */
public record JiraSyncSettingsResponse(int interactionThresholdSeconds) {
}
