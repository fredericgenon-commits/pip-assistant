package com.utmost.lu.pipassistant.domain.model;

/**
 * Diff-derived status of a requirement within a PIP, computed at each Excel import by
 * comparing the new file against the previous version's snapshot. Distinct from the
 * workflow {@code status} (TODO/IN_PROGRESS/DONE) which is edited in the app.
 */
public enum RequirementPipStatus {

    /** First appearance (all REQ of the very first import, or a REQ added in a later version). */
    NEW("New"),
    /** Present in both versions, identical content and same priority. */
    UNCHANGED("Unchanged"),
    /** Present in both versions, a content field changed. */
    CHANGED("Changed"),
    /** Present in both versions, only the priority (order) changed. */
    PRIORITY_CHANGED("Priority changed"),
    /** Present in the previous version but gone from the new file; kept visible in the PIP. */
    REMOVED_FROM_PIP("Removed from PIP"),
    /** A REQ row missing a mandatory field (TCM key, TCM/REQ description). */
    MISSING_DATA("Missing data in import file");

    private final String label;

    RequirementPipStatus(String label) {
        this.label = label;
    }

    /** Human-readable label for display. */
    public String label() {
        return label;
    }
}
