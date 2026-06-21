package com.utmost.lu.pipassistant.domain.model;

import java.math.BigDecimal;
import java.util.Map;

/**
 * One REQ row parsed from the PM Excel file, in file order. Workloads are keyed by team
 * name (only teams with a value are present). {@code missingData} is true when a mandatory
 * field (TCM key, TCM description or REQ description) is absent.
 */
public record ParsedRequirement(
        int order,
        String tcmKey,
        String tcmDescription,
        String reqKey,
        String reqDescription,
        String pmComment,
        Map<String, BigDecimal> workloadsByTeam,
        boolean missingData) {
}
