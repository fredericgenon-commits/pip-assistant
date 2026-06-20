package com.utmost.lu.pipassistant.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.Team;

/** Aggregated read model for the PIP Details screen. */
public record PipDetailView(
        Pip pip,
        List<Team> teams,
        List<RequirementRow> requirements,
        Map<Long, BigDecimal> capacities) {

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
            Map<Long, BigDecimal> workloads,
            Map<Long, String> comments) {
    }
}
