package com.utmost.lu.pipassistant.domain.port;

import java.util.List;

/** Outbound port for persisting and reading JIRA-computed Team Status per requirement and team. */
public interface RequirementBacklogRepository {

    /**
     * Insert or update the Team Status for a (requirement, team) pair.
     * Pass {@code null} to clear a previously stored status.
     */
    void upsertTeamStatus(Long requirementId, Long teamId, String teamStatus);

    /**
     * Returns all Team Status entries for requirements belonging to the given PIP.
     */
    List<TeamBacklogEntry> findByPip(Long pipId);

    /** One row of the requirement_backlog table. */
    record TeamBacklogEntry(Long requirementId, Long teamId, String teamStatus) {}
}
