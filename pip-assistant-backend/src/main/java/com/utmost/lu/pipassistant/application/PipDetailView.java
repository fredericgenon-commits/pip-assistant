package com.utmost.lu.pipassistant.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.Team;

/** Aggregated read model for the PIP Details screen. */
public record PipDetailView(
        Pip pip,
        List<Team> teams,
        List<RequirementRow> requirements,
        Map<Long, BigDecimal> capacities,
        /** Metadata of the latest Excel import, or null if none has been made yet. */
        LastImport lastImport) {

    public record LastImport(int versionNo, String originalFilename, Instant importedAt) {}

    /** One requirement row, with workloads and dev comments keyed by team id. */
    public record RequirementRow(
            Long id,
            Long projectId,
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
            /** team id -> true when the cell is owned by the JIRA sync (read-only in the UI) */
            Map<Long, Boolean> jiraLocked,
            /** team id -> JIRA-computed Team Status (null when no data) */
            Map<Long, String> teamStatuses) {
    }
}
