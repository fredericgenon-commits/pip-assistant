package com.utmost.lu.pipassistant.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utmost.lu.pipassistant.domain.model.Requirement;
import com.utmost.lu.pipassistant.domain.model.Team;
import com.utmost.lu.pipassistant.domain.model.Workload;
import com.utmost.lu.pipassistant.domain.port.JiraBacklogPort;
import com.utmost.lu.pipassistant.domain.port.JiraPort;
import com.utmost.lu.pipassistant.domain.port.PipDetailRepository;
import com.utmost.lu.pipassistant.domain.port.PipRepository;
import com.utmost.lu.pipassistant.domain.port.RequirementBacklogRepository;
import com.utmost.lu.pipassistant.domain.port.TeamRepository;
import com.utmost.lu.pipassistant.domain.service.BacklogCalculator;
import com.utmost.lu.pipassistant.infrastructure.config.JiraProperties;

/**
 * Orchestrates the JIRA synchronisation for a PIP: fetches REQ statuses and dev-ticket backlogs,
 * persists the results, and enforces a configurable TTL to avoid hammering JIRA.
 */
@Service
public class JiraSyncService {

    private final JiraPort jiraPort;
    private final JiraBacklogPort jiraBacklogPort;
    private final BacklogCalculator backlogCalculator = new BacklogCalculator();
    private final PipDetailRepository detailRepository;
    private final RequirementBacklogRepository backlogRepository;
    private final TeamRepository teamRepository;
    private final PipRepository pipRepository;
    private final JiraProperties jiraProperties;

    /** In-memory last-sync timestamps per PIP id. Reset on restart — acceptable for dev use. */
    private final Map<Long, Instant> lastSyncTimes = new ConcurrentHashMap<>();

    public JiraSyncService(
            JiraPort jiraPort,
            JiraBacklogPort jiraBacklogPort,
            PipDetailRepository detailRepository,
            RequirementBacklogRepository backlogRepository,
            TeamRepository teamRepository,
            PipRepository pipRepository,
            JiraProperties jiraProperties) {
        this.jiraPort = jiraPort;
        this.jiraBacklogPort = jiraBacklogPort;
        this.detailRepository = detailRepository;
        this.backlogRepository = backlogRepository;
        this.teamRepository = teamRepository;
        this.pipRepository = pipRepository;
        this.jiraProperties = jiraProperties;
    }

    /**
     * Synchronises a PIP with JIRA: for each requirement, fetches the JIRA status and the
     * dev-ticket backlog, then persists the results.
     * <p>
     * Returns immediately (synced=0) when the TTL has not yet elapsed since the last sync.
     *
     * @throws PipNotFoundException when the PIP does not exist
     */
    @Transactional
    public JiraSyncResult sync(Long pipId) {
        pipRepository.findById(pipId).orElseThrow(() -> new PipNotFoundException(pipId));

        // TTL guard: skip JIRA calls when synced recently.
        Instant lastSync = lastSyncTimes.get(pipId);
        long ttlSeconds = (long) jiraProperties.getSyncTtlMinutes() * 60;
        if (lastSync != null && lastSync.plusSeconds(ttlSeconds).isAfter(Instant.now())) {
            return new JiraSyncResult(0, 0, List.of());
        }

        List<Team> teams = teamRepository.findAllOrdered();
        Map<String, Long> jiraTeamToId = buildJiraTeamToId(teams);

        // Pre-load existing workloads so we can detect previously JIRA-locked cells.
        Map<Long, Map<Long, Boolean>> existingJiraLocked = loadExistingJiraLocked(pipId);

        List<Requirement> requirements = detailRepository.findRequirementsByPip(pipId);
        int synced = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (Requirement req : requirements) {
            try {
                syncRequirement(req, teams, jiraTeamToId, existingJiraLocked);
                synced++;
            } catch (Exception e) {
                failed++;
                errors.add(req.reqKey() + ": " + e.getMessage());
            }
        }

        lastSyncTimes.put(pipId, Instant.now());
        return new JiraSyncResult(synced, failed, errors);
    }

    private void syncRequirement(
            Requirement req,
            List<Team> teams,
            Map<String, Long> jiraTeamToId,
            Map<Long, Map<Long, Boolean>> existingJiraLocked) {

        // 1. Update REQ status from JIRA.
        jiraPort.fetchStatus(req.reqKey()).ifPresent(
                status -> detailRepository.updateRequirementStatus(req.id(), status));

        // 2. Fetch dev tickets and compute backlog per team.
        var tickets = jiraBacklogPort.fetchDevTickets(req.reqKey());
        Map<Long, BacklogCalculator.TeamBacklogResult> results =
                backlogCalculator.compute(tickets, jiraTeamToId);

        // 3. Persist backlog results for every team.
        for (Team team : teams) {
            Long teamId = team.id();
            BacklogCalculator.TeamBacklogResult result = results.get(teamId);

            if (result != null && result.storyPoints() > 0) {
                detailRepository.upsertWorkloadFromJira(req.id(), teamId,
                        BigDecimal.valueOf(result.storyPoints()));
            } else {
                // If this cell was previously locked by JIRA, release it.
                boolean wasLocked = existingJiraLocked
                        .getOrDefault(req.id(), Map.of())
                        .getOrDefault(teamId, false);
                if (wasLocked) {
                    detailRepository.unlockWorkloadFromJira(req.id(), teamId);
                }
            }

            String teamStatus = result != null ? result.teamStatus() : null;
            backlogRepository.upsertTeamStatus(req.id(), teamId, teamStatus);
        }
    }

    /** Builds the JIRA-team-value → team-id map from the configured team mapping and seeded teams. */
    private Map<String, Long> buildJiraTeamToId(List<Team> teams) {
        Map<String, Long> teamsByName = new HashMap<>();
        for (Team t : teams) {
            teamsByName.put(t.name(), t.id());
        }

        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<String, String> entry : jiraProperties.getTeamMapping().entrySet()) {
            Long teamId = teamsByName.get(entry.getValue());
            if (teamId != null) {
                result.put(entry.getKey(), teamId);
            }
        }
        return result;
    }

    private Map<Long, Map<Long, Boolean>> loadExistingJiraLocked(Long pipId) {
        Map<Long, Map<Long, Boolean>> map = new HashMap<>();
        for (Workload w : detailRepository.findWorkloadsByPip(pipId)) {
            if (w.jiraLocked()) {
                map.computeIfAbsent(w.requirementId(), k -> new HashMap<>())
                        .put(w.teamId(), true);
            }
        }
        return map;
    }
}
