package com.utmost.lu.pipassistant.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Bulk edit applied by the single "Save" button of the PIP Details screen. */
public record SavePipDetailCommand(
        List<RequirementEdit> requirements,
        Map<Long, BigDecimal> capacities) {

    /** Edits for one requirement row (TCM/REQ keys are read-only and not included). */
    public record RequirementEdit(
            Long id,
            String tcmDescription,
            String description,
            String status,
            String pmComment,
            Map<Long, BigDecimal> workloads,
            Map<Long, String> comments) {
    }
}
