package com.utmost.lu.pipassistant.application;

import java.util.Map;

/** Lightweight read model for a single requirement's JIRA-derived fields. */
public record BacklogPatch(
        Long requirementId,
        String status,
        Map<Long, String> teamStatuses,
        Map<Long, Boolean> jiraLocked,
        Map<Long, String> workloads) {
}
