package com.utmost.lu.pipassistant.infrastructure.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utmost.lu.pipassistant.infrastructure.config.JiraProperties;
import com.utmost.lu.pipassistant.infrastructure.web.dto.JiraSyncSettingsResponse;

/** Exposes JIRA-related configuration values needed by the frontend. */
@RestController
@RequestMapping("/api")
public class JiraSyncSettingsController {

    private final JiraProperties jiraProperties;

    public JiraSyncSettingsController(JiraProperties jiraProperties) {
        this.jiraProperties = jiraProperties;
    }

    @GetMapping("/jira-sync-settings")
    public JiraSyncSettingsResponse settings() {
        return new JiraSyncSettingsResponse(jiraProperties.getInteractionSyncThresholdSeconds());
    }
}
