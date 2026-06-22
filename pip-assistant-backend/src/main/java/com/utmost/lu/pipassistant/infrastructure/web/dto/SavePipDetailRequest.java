package com.utmost.lu.pipassistant.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.utmost.lu.pipassistant.application.SavePipDetailCommand;

// Workloads are carried as cell text (a number, "TBD", or empty); capacities stay numeric.

/** Bulk save payload from the PIP Details "Save" button. */
public record SavePipDetailRequest(
        List<RequirementEditRequest> requirements,
        Map<Long, BigDecimal> capacities) {

    public record RequirementEditRequest(
            Long id,
            String tcmDescription,
            String description,
            String status,
            String pmComment,
            Map<Long, String> workloads,
            Map<Long, String> comments) {
    }

    public SavePipDetailCommand toCommand() {
        List<SavePipDetailCommand.RequirementEdit> edits = requirements == null ? List.of()
                : requirements.stream()
                        .map(r -> new SavePipDetailCommand.RequirementEdit(
                                r.id(), r.tcmDescription(), r.description(), r.status(),
                                r.pmComment(), r.workloads(), r.comments()))
                        .toList();
        return new SavePipDetailCommand(edits, capacities);
    }
}
