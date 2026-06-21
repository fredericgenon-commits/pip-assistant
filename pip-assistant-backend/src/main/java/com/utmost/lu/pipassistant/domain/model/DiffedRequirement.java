package com.utmost.lu.pipassistant.domain.model;

/** A parsed requirement enriched with its computed priority and PIP status. */
public record DiffedRequirement(
        ParsedRequirement parsed,
        int priority,
        RequirementPipStatus status) {
}
