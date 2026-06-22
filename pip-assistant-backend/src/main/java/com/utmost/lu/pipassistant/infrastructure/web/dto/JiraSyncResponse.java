package com.utmost.lu.pipassistant.infrastructure.web.dto;

import java.util.List;

import com.utmost.lu.pipassistant.application.JiraSyncResult;

/** HTTP response for a JIRA synchronisation run. */
public record JiraSyncResponse(int synced, int failed, List<String> errors) {

    public static JiraSyncResponse from(JiraSyncResult result) {
        return new JiraSyncResponse(result.synced(), result.failed(), result.errors());
    }
}
