package com.utmost.lu.pipassistant.domain.port;

import java.math.BigDecimal;
import java.util.List;

import com.utmost.lu.pipassistant.domain.model.DevComment;
import com.utmost.lu.pipassistant.domain.model.PipCapacity;
import com.utmost.lu.pipassistant.domain.model.Project;
import com.utmost.lu.pipassistant.domain.model.Requirement;
import com.utmost.lu.pipassistant.domain.model.Workload;

/**
 * Outbound port for the PIP detail aggregate (projects, requirements, workloads, dev
 * comments and capacities of a PIP).
 */
public interface PipDetailRepository {

    List<Project> findProjectsByPip(Long pipId);

    List<Requirement> findRequirementsByPip(Long pipId);

    List<Workload> findWorkloadsByPip(Long pipId);

    List<DevComment> findDevCommentsByPip(Long pipId);

    List<PipCapacity> findCapacitiesByPip(Long pipId);

    void updateProjectDescription(Long projectId, String description);

    void updateRequirement(Long requirementId, String description, String pmComment);

    /** Persists the JIRA-sourced status for a single requirement. */
    void updateRequirementStatus(Long requirementId, String status);

    /**
     * Insert or update a team's workload on a requirement. When {@code tbd} is true the cell
     * is marked "To Be Defined" and {@code estimate} is ignored (stored as null).
     */
    void upsertWorkload(Long requirementId, Long teamId, BigDecimal estimate, boolean tbd);

    /**
     * Write story points from the JIRA backlog sync: sets the estimate, clears TBD, sets
     * jira_locked=true and manual_override=true (so imports cannot overwrite the JIRA value).
     */
    void upsertWorkloadFromJira(Long requirementId, Long teamId, BigDecimal storyPoints);

    /**
     * Release the JIRA lock on a workload cell when JIRA no longer has RFI tickets for this
     * (requirement, team) pair: sets jira_locked=false and manual_override=false (so the next
     * import may overwrite the cell again). Only acts when the cell is currently jira_locked.
     */
    void unlockWorkloadFromJira(Long requirementId, Long teamId);

    /** Updates only the status of a requirement (used by the JIRA sync). */
    void updateRequirementStatus(Long requirementId, String status);

    /** Insert or update a team's dev comment on a requirement. */
    void upsertDevComment(Long requirementId, Long teamId, String text);

    /** Insert or update a team's capacity for a PIP. */
    void upsertCapacity(Long pipId, Long teamId, BigDecimal capacity);

    /**
     * Creates a requirement under the PIP, creating/linking its project by TCM key.
     * Interim entry point used by tests and the future Excel/JIRA import.
     */
    Requirement createRequirement(
            Long pipId,
            String tcmKey,
            String tcmDescription,
            String reqKey,
            String description,
            String status,
            String pmComment);
}
