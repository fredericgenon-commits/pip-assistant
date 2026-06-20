package com.utmost.lu.pipassistant.domain.model;

/** A project (TCM): a TCM key + its description, belonging to a PIP. */
public record Project(Long id, String tcmKey, String description, Long pipId) {
}
