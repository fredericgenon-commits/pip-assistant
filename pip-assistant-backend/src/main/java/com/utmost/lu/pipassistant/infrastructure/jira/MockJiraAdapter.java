package com.utmost.lu.pipassistant.infrastructure.jira;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.utmost.lu.pipassistant.domain.model.DevTicket;
import com.utmost.lu.pipassistant.domain.port.JiraBacklogPort;
import com.utmost.lu.pipassistant.domain.port.JiraPort;

/**
 * Mock JIRA adapter for local development (profile {@code jira-mock}).
 * Returns deterministic fake data so the UI can be exercised without a real JIRA instance.
 */
@Component
@Profile("jira-mock")
public class MockJiraAdapter implements JiraPort, JiraBacklogPort {

    @Override
    public Optional<String> fetchStatus(String issueKey) {
        return Optional.of("In Progress");
    }

    /**
     * Returns a fixed set of dev tickets regardless of the REQ key, to exercise all UI states:
     * <ul>
     *   <li>Core: 2 DEV tickets in RFI (3 SP each) → cell locked at 6 SP; 1 TA in Open → "TA todo"</li>
     *   <li>Portal: 1 DEV ticket in TBE (5 SP) → cell not locked; Team Status "To be estimated"</li>
     *   <li>Process: 1 DEV ticket Done (8 SP) → cell not locked; Team Status "Done"</li>
     * </ul>
     */
    @Override
    public List<DevTicket> fetchDevTickets(String reqKey) {
        return List.of(
                new DevTicket("DEV-100", "Implement " + reqKey + " core flow",
                        "Ready for implementation", "Project", "Core", 3),
                new DevTicket("DEV-101", "Implement " + reqKey + " core edge cases",
                        "Ready for implementation", "Project", "Core", 3),
                new DevTicket("DEV-102", "[Technical Analysis] " + reqKey + " core",
                        "Open", "Project", "Core", null),
                new DevTicket("DEV-103", "Implement " + reqKey + " portal UI",
                        "To be estimated", "Project", "Portal", 5),
                new DevTicket("DEV-104", "Implement " + reqKey + " process integration",
                        "Done", "Project", "Process", 8)
        );
    }
}
