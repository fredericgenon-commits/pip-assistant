package com.utmost.lu.pipassistant.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.utmost.lu.pipassistant.domain.model.Requirement;
import com.utmost.lu.pipassistant.domain.port.JiraPort;
import com.utmost.lu.pipassistant.domain.port.PipDetailRepository;
import com.utmost.lu.pipassistant.domain.port.PipRepository;
import com.utmost.lu.pipassistant.infrastructure.config.JiraProperties;

/** Synchronises the JIRA status for every requirement of a PIP. */
@Service
public class JiraSyncService {

    private final JiraPort jiraPort;
    private final PipDetailRepository detailRepository;
    private final PipRepository pipRepository;
    private final JiraProperties jiraProperties;

    private final Map<Long, Instant> lastSyncTimes = new ConcurrentHashMap<>();

    public JiraSyncService(JiraPort jiraPort, PipDetailRepository detailRepository,
                           PipRepository pipRepository, JiraProperties jiraProperties) {
        this.jiraPort = jiraPort;
        this.detailRepository = detailRepository;
        this.pipRepository = pipRepository;
        this.jiraProperties = jiraProperties;
    }

    /**
     * Fetches the JIRA status for every requirement of the given PIP and persists it.
     * Skips the JIRA calls if a sync was performed within the configured TTL window,
     * preventing redundant requests when the page is reloaded frequently.
     * Partial failures (JIRA unreachable for one ticket) do not abort the whole sync.
     */
    public JiraSyncResult sync(Long pipId) {
        pipRepository.findById(pipId).orElseThrow(() -> new PipNotFoundException(pipId));

        Instant lastSync = lastSyncTimes.get(pipId);
        if (lastSync != null && lastSync.plusSeconds((long) jiraProperties.getSyncTtlMinutes() * 60)
                .isAfter(Instant.now())) {
            return new JiraSyncResult(0, 0, List.of());
        }

        List<Requirement> requirements = detailRepository.findRequirementsByPip(pipId);

        int synced = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        for (Requirement req : requirements) {
            try {
                var statusOpt = jiraPort.fetchStatus(req.reqKey());
                if (statusOpt.isPresent()) {
                    detailRepository.updateRequirementStatus(req.id(), statusOpt.get());
                    synced++;
                } else {
                    failed++;
                    errors.add(req.reqKey() + ": not found in JIRA");
                }
            } catch (Exception e) {
                failed++;
                errors.add(req.reqKey() + ": " + e.getMessage());
            }
        }

        lastSyncTimes.put(pipId, Instant.now());
        return new JiraSyncResult(synced, failed, errors);
    }
}
