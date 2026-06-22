package com.utmost.lu.pipassistant.infrastructure.web.dto;

/**
 * Frontend configuration for the auto-sync behaviour: how long (in seconds) after the last
 * sync must pass before a user interaction triggers a background resync.
 */
public record JiraSyncSettingsResponse(int interactionThresholdSeconds) {
}
