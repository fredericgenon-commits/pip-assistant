package com.utmost.lu.pipassistant.infrastructure.web.dto;

import java.util.Map;

import com.utmost.lu.pipassistant.application.PipDetailView;

/** One requirement row, with workloads and dev comments keyed by team id. */
public record RequirementRowResponse(
        Long id,
        String tcmKey,
        String tcmDescription,
        String reqKey,
        String description,
        String status,
        String pmComment,
        Integer priority,
        String pipStatus,
        Map<Long, String> workloads,
        Map<Long, String> comments,
        String reqUrl,
        String tcmUrl) {

    public static RequirementRowResponse from(PipDetailView.RequirementRow row, String jiraBaseUrl) {
        return new RequirementRowResponse(
                row.id(),
                row.tcmKey(),
                row.tcmDescription(),
                row.reqKey(),
                row.description(),
                row.status(),
                row.pmComment(),
                row.priority(),
                row.pipStatus(),
                row.workloads(),
                row.comments(),
                buildUrl(jiraBaseUrl, row.reqKey()),
                buildUrl(jiraBaseUrl, row.tcmKey()));
    }

    private static String buildUrl(String baseUrl, String key) {
        if (baseUrl == null || baseUrl.isBlank() || key == null) {
            return null;
        }
        return baseUrl.stripTrailing() + "/browse/" + key;
    }
}
