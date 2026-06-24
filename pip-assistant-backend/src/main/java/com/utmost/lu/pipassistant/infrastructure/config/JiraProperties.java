package com.utmost.lu.pipassistant.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** JIRA connection settings bound from {@code pip.jira.*} in application config. */
@Component
@ConfigurationProperties(prefix = "pip.jira")
public class JiraProperties {

    private String baseUrl = "";
    private String apiToken = "";
    private int syncTtlMinutes = 15;
    private int interactionSyncThresholdSeconds = 30;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiToken() { return apiToken; }
    public void setApiToken(String apiToken) { this.apiToken = apiToken; }

    public int getSyncTtlMinutes() { return syncTtlMinutes; }
    public void setSyncTtlMinutes(int syncTtlMinutes) { this.syncTtlMinutes = syncTtlMinutes; }

    public int getInteractionSyncThresholdSeconds() { return interactionSyncThresholdSeconds; }
    public void setInteractionSyncThresholdSeconds(int s) { this.interactionSyncThresholdSeconds = s; }
}
