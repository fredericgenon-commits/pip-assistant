package com.utmost.lu.pipassistant.domain.model;

import java.math.BigDecimal;
import java.util.Map;

/**
 * A REQ as stored in a previous import version's snapshot, used as the diff baseline.
 * Workloads are keyed by team name to match {@link ParsedRequirement}.
 */
public record SnapshotRequirement(
        String reqKey,
        String tcmKey,
        String tcmDescription,
        String reqDescription,
        String pmComment,
        Integer priority,
        Map<String, BigDecimal> workloadsByTeam) {
}
