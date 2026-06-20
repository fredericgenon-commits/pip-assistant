package com.utmost.lu.pipassistant.infrastructure.web.dto;

/** Interim create payload (tests / future Excel-JIRA import). */
public record CreateRequirementRequest(
        String tcmKey,
        String tcmDescription,
        String reqKey,
        String description,
        String status,
        String pmComment) {
}
