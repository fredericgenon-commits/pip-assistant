package com.utmost.lu.pipassistant.domain.model;

/**
 * A requirement (REQ) under a project. {@code status} is a free String validated against
 * the configurable list of requirement statuses. {@code priority} and {@code pipStatus}
 * are derived by the Excel import diff ({@code pipStatus} is a {@link RequirementPipStatus}
 * name; both are null until the first import).
 */
public record Requirement(
        Long id,
        String reqKey,
        String description,
        String status,
        String pmComment,
        Long projectId,
        Integer priority,
        String pipStatus) {
}
