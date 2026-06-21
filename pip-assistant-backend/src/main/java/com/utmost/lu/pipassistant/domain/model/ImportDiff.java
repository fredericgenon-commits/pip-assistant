package com.utmost.lu.pipassistant.domain.model;

import java.util.List;

/**
 * Outcome of comparing a freshly parsed file against the previous version's snapshot:
 * the current requirements (with priority + status) and the keys of requirements that
 * disappeared (to be marked {@code REMOVED_FROM_PIP}).
 */
public record ImportDiff(
        List<DiffedRequirement> current,
        List<String> removedReqKeys) {
}
