package com.utmost.lu.pipassistant.domain.model;

/**
 * A requirement (REQ) under a project. {@code status} is a free String validated against
 * the configurable list of requirement statuses.
 */
public record Requirement(
        Long id,
        String reqKey,
        String description,
        String status,
        String pmComment,
        Long projectId) {
}
