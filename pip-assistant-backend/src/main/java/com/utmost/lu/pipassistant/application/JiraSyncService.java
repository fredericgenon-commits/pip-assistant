package com.utmost.lu.pipassistant.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.utmost.lu.pipassistant.domain.model.Requirement;
import com.utmost.lu.pipassistant.domain.port.JiraPort;
import com.utmost.lu.pipassistant.domain.port.PipDetailRepository;
import com.utmost.lu.pipassistant.domain.port.PipRepository;

/** Synchronises the JIRA status for every requirement of a PIP. */
@Service
public class JiraSyncService {

    private final JiraPort jiraPort;
    private final PipDetailRepository detailRepository;
    private final PipRepository pipRepository;

    public JiraSyncService(JiraPort jiraPort, PipDetailRepository detailRepository,
                           PipRepository pipRepository) {
        this.jiraPort = jiraPort;
        this.detailRepository = detailRepository;
        this.pipRepository = pipRepository;
    }

    /**
     * Fetches the JIRA status for every requirement of the given PIP and persists it.
     * Partial failures (JIRA unreachable for one ticket) do not abort the whole sync.
     */
    public JiraSyncResult sync(Long pipId) {
        pipRepository.findById(pipId).orElseThrow(() -> new PipNotFoundException(pipId));
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

        return new JiraSyncResult(synced, failed, errors);
    }
}
