package com.utmost.lu.pipassistant.infrastructure.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utmost.lu.pipassistant.application.JiraSyncResult;
import com.utmost.lu.pipassistant.application.JiraSyncService;
import com.utmost.lu.pipassistant.infrastructure.config.JiraProperties;
import com.utmost.lu.pipassistant.infrastructure.web.dto.JiraSyncSettingsResponse;

@RestController
@RequestMapping("/api")
public class JiraSyncController {

    private final JiraSyncService syncService;
    private final JiraProperties jiraProperties;

    public JiraSyncController(JiraSyncService syncService, JiraProperties jiraProperties) {
        this.syncService = syncService;
        this.jiraProperties = jiraProperties;
    }

    /** Trigger a JIRA sync for a PIP. Respects the backend TTL. */
    @PostMapping("/pips/{pipId}/jira-sync")
    public JiraSyncResult sync(@PathVariable Long pipId) {
        return syncService.sync(pipId);
    }

    /** Returns frontend configuration for the interaction-triggered auto-sync behaviour. */
    @GetMapping("/jira-sync-settings")
    public JiraSyncSettingsResponse settings() {
        return new JiraSyncSettingsResponse(jiraProperties.getInteractionSyncThresholdSeconds());
    }
}
