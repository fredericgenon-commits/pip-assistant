package com.utmost.lu.pipassistant.infrastructure.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JIRA connection and sync configuration bound from {@code pip.jira} in application config.
 */
@Component
@ConfigurationProperties(prefix = "pip.jira")
public class JiraProperties {

    private String baseUrl = "";
    private String apiToken = "";

    /** Backend TTL: JIRA is not called again within this many minutes for the same PIP. */
    private int syncTtlMinutes = 10;

    /**
     * Frontend interaction threshold: after this many seconds since the last sync, the next
     * user interaction on the PIP detail page triggers a background resync.
     */
    private int interactionSyncThresholdSeconds = 60;

    /** Custom field IDs for the JIRA fields that carry dev ticket metadata. */
    private Fields field = new Fields();

    /**
     * Maps JIRA "team" field values to team names known to the system.
     * Example: {@code "Core Team" -> "Core"}. Unmapped teams are ignored.
     */
    private Map<String, String> teamMapping = new LinkedHashMap<>();

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getApiToken() { return apiToken; }
    public void setApiToken(String apiToken) { this.apiToken = apiToken; }

    public int getSyncTtlMinutes() { return syncTtlMinutes; }
    public void setSyncTtlMinutes(int syncTtlMinutes) { this.syncTtlMinutes = syncTtlMinutes; }

    public int getInteractionSyncThresholdSeconds() { return interactionSyncThresholdSeconds; }
    public void setInteractionSyncThresholdSeconds(int s) { this.interactionSyncThresholdSeconds = s; }

    public Fields getField() { return field; }
    public void setField(Fields field) { this.field = field; }

    public Map<String, String> getTeamMapping() { return teamMapping; }
    public void setTeamMapping(Map<String, String> teamMapping) { this.teamMapping = teamMapping; }

    /** JIRA custom field IDs used when querying dev tickets. */
    public static class Fields {
        /** Field carrying the delivery method (e.g. "Project", "Maintenance"). */
        private String deliveryMethod = "customfield_10000";
        /** Field carrying the team name. */
        private String team = "customfield_10001";
        /** Story points field (typically customfield_10016 on most JIRA instances). */
        private String storyPoints = "customfield_10016";

        public String getDeliveryMethod() { return deliveryMethod; }
        public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }

        public String getTeam() { return team; }
        public void setTeam(String team) { this.team = team; }

        public String getStoryPoints() { return storyPoints; }
        public void setStoryPoints(String storyPoints) { this.storyPoints = storyPoints; }
    }
}
