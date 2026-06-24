package com.utmost.lu.pipassistant.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.utmost.lu.pipassistant.domain.model.DevTicket;

class BacklogCalculatorTest {

    private static final Map<String, Long> TEAM_MAP = Map.of(
            "Core", 1L,
            "Portal", 2L,
            "Process", 3L);

    private final BacklogCalculator calc = new BacklogCalculator();

    private static DevTicket dev(String key, String summary, String status, String team, Integer sp) {
        return new DevTicket(key, summary, status, "Project", team, sp);
    }

    private static DevTicket ta(String key, String prefix, String status, String team) {
        return new DevTicket(key, prefix + " some analysis", status, "Project", team, null);
    }

    // ── Team Status priority 1: TA non-abandoned in open/tbe/rfi → "TA todo" ──

    @Test
    void teamStatus_taTodoWhenTaIsOpen() {
        var result = calc.compute(List.of(
                ta("TA-1", "[Technical Analysis]", "Open", "Core"),
                dev("DEV-1", "Do something", "Ready for implementation", "Core", 5)
        ), TEAM_MAP);

        assertThat(result.get(1L).teamStatus()).isEqualTo("TA todo");
    }

    @Test
    void teamStatus_taTodoWhenTaIsRfi() {
        var result = calc.compute(List.of(
                ta("TA-1", "[technical-analysis]", "Ready for implementation", "Core")
        ), TEAM_MAP);

        assertThat(result.get(1L).teamStatus()).isEqualTo("TA todo");
    }

    @Test
    void teamStatus_taTodoWhenTaIsTbe() {
        var result = calc.compute(List.of(
                ta("TA-1", "[tech. analysis]", "To be estimated", "Core")
        ), TEAM_MAP);

        assertThat(result.get(1L).teamStatus()).isEqualTo("TA todo");
    }

    @Test
    void teamStatus_taTodoPrecedesOtherRules() {
        var result = calc.compute(List.of(
                ta("TA-1", "[Technical_Analysis]", "Open", "Core"),
                dev("DEV-1", "Done ticket", "Done", "Core", 3)
        ), TEAM_MAP);

        // Even though all DEV are done (rule 5), TA in Open wins (rule 1).
        assertThat(result.get(1L).teamStatus()).isEqualTo("TA todo");
    }

    // ── Priority 2: TA in progress → "TA ongoing" ──

    @Test
    void teamStatus_taOngoingWhenTaIsInProgress() {
        var result = calc.compute(List.of(
                ta("TA-1", "[Technical Analysis]", "In progress", "Core"),
                dev("DEV-1", "DEV", "Done", "Core", 3)
        ), TEAM_MAP);

        assertThat(result.get(1L).teamStatus()).isEqualTo("TA ongoing");
    }

    @Test
    void teamStatus_taOngoingCaseInsensitive() {
        var result = calc.compute(List.of(
                ta("TA-1", "[technical analysis]", "IN PROGRESS", "Core")
        ), TEAM_MAP);

        assertThat(result.get(1L).teamStatus()).isEqualTo("TA ongoing");
    }

    // ── Priority 3: any DEV in tbe → "To be estimated" ──

    @Test
    void teamStatus_toBeEstimated() {
        var result = calc.compute(List.of(
                dev("DEV-1", "DEV", "To be estimated", "Core", 5),
                dev("DEV-2", "DEV", "Ready for implementation", "Core", 3)
        ), TEAM_MAP);

        assertThat(result.get(1L).teamStatus()).isEqualTo("To be estimated");
    }

    // ── Priority 4: all DEV non-abandoned in rfi → "Ready" ──

    @Test
    void teamStatus_readyWhenAllRfi() {
        var result = calc.compute(List.of(
                dev("DEV-1", "DEV", "Ready for implementation", "Core", 5),
                dev("DEV-2", "DEV", "Ready for implementation", "Core", 3)
        ), TEAM_MAP);

        assertThat(result.get(1L).teamStatus()).isEqualTo("Ready");
    }

    @Test
    void teamStatus_readyIgnoresAbandonned() {
        var result = calc.compute(List.of(
                dev("DEV-1", "DEV", "Ready for implementation", "Core", 5),
                dev("DEV-2", "DEV", "Abandonned", "Core", 3)
        ), TEAM_MAP);

        assertThat(result.get(1L).teamStatus()).isEqualTo("Ready");
    }

    // ── Priority 5: all DEV done → "Done" ──

    @Test
    void teamStatus_doneWhenAllDone() {
        var result = calc.compute(List.of(
                dev("DEV-1", "DEV", "Done", "Core", 5),
                dev("DEV-2", "DEV", "Closed", "Core", 3)
        ), TEAM_MAP);

        assertThat(result.get(1L).teamStatus()).isEqualTo("Done");
    }

    // ── No status (null) cases ──

    @Test
    void teamStatus_nullWhenNoTickets() {
        var result = calc.compute(List.of(), TEAM_MAP);
        assertThat(result).isEmpty();
    }

    @Test
    void teamStatus_nullWhenAllAbandoned() {
        var result = calc.compute(List.of(
                dev("DEV-1", "DEV", "Abandonned", "Core", 5)
        ), TEAM_MAP);

        assertThat(result.get(1L).teamStatus()).isNull();
    }

    @Test
    void teamStatus_nullWhenOnlyOpenDevTickets() {
        var result = calc.compute(List.of(
                dev("DEV-1", "DEV", "Open", "Core", null)
        ), TEAM_MAP);

        // "Open" DEV matches no priority rule → null
        assertThat(result.get(1L).teamStatus()).isNull();
    }

    @Test
    void teamStatus_nullWhenOnlyAbandonedTa() {
        var result = calc.compute(List.of(
                ta("TA-1", "[Technical Analysis]", "Abandonned", "Core")
        ), TEAM_MAP);

        assertThat(result.get(1L).teamStatus()).isNull();
    }

    // ── Backlog SP calculation ──

    @Test
    void backlogSp_sumsOnlyRfiNonTa() {
        var result = calc.compute(List.of(
                dev("DEV-1", "DEV", "Ready for implementation", "Core", 5),
                dev("DEV-2", "DEV", "Ready for implementation", "Core", 3),
                dev("DEV-3", "DEV", "To be estimated", "Core", 8),    // not RFI → excluded
                ta("TA-1", "[Technical Analysis]", "Ready for implementation", "Core"), // TA → excluded
                dev("DEV-4", "DEV", "Abandonned", "Core", 10)         // abandoned → excluded
        ), TEAM_MAP);

        assertThat(result.get(1L).storyPoints()).isEqualTo(8);
    }

    @Test
    void backlogSp_zeroWhenNoRfiTickets() {
        var result = calc.compute(List.of(
                dev("DEV-1", "DEV", "Done", "Core", 5)
        ), TEAM_MAP);

        assertThat(result.get(1L).storyPoints()).isEqualTo(0);
    }

    @Test
    void backlogSp_nullSpCountsAsZero() {
        var result = calc.compute(List.of(
                dev("DEV-1", "DEV", "Ready for implementation", "Core", null)
        ), TEAM_MAP);

        assertThat(result.get(1L).storyPoints()).isEqualTo(0);
    }

    // ── Filtering ──

    @Test
    void ignoresTicketsWithUnknownTeam() {
        var result = calc.compute(List.of(
                dev("DEV-1", "DEV", "Ready for implementation", "UnknownTeam", 5)
        ), TEAM_MAP);

        assertThat(result).isEmpty();
    }

    @Test
    void ignoresTicketsWithNonProjectDeliveryMethod() {
        var ticket = new DevTicket("DEV-1", "DEV", "Ready for implementation", "Maintenance", "Core", 5);
        var result = calc.compute(List.of(ticket), TEAM_MAP);

        assertThat(result).isEmpty();
    }

    @Test
    void deliveryMethodIsCaseInsensitive() {
        var ticket = new DevTicket("DEV-1", "DEV", "Ready for implementation", "PROJECT", "Core", 5);
        var result = calc.compute(List.of(ticket), TEAM_MAP);

        assertThat(result.get(1L).storyPoints()).isEqualTo(5);
    }

    @Test
    void taPrefixDetectionIsCaseInsensitive() {
        List<String> prefixes = List.of(
                "[Technical Analysis] task",
                "[technical_analysis] task",
                "[TECHNICAL-ANALYSIS] task",
                "[Tech. Analysis] task");

        for (String summary : prefixes) {
            var ticket = new DevTicket("TA-1", summary, "Open", "Project", "Core", null);
            var result = calc.compute(List.of(ticket), TEAM_MAP);
            assertThat(result.get(1L).teamStatus())
                    .as("summary: " + summary)
                    .isEqualTo("TA todo");
        }
    }

    // ── Multi-team ──

    @Test
    void computesResultsPerTeamIndependently() {
        var result = calc.compute(List.of(
                dev("DEV-1", "DEV", "Ready for implementation", "Core", 3),
                dev("DEV-2", "DEV", "To be estimated", "Portal", 5),
                dev("DEV-3", "DEV", "Done", "Process", 8)
        ), TEAM_MAP);

        assertThat(result.get(1L).teamStatus()).isEqualTo("Ready");
        assertThat(result.get(1L).storyPoints()).isEqualTo(3);

        assertThat(result.get(2L).teamStatus()).isEqualTo("To be estimated");
        assertThat(result.get(2L).storyPoints()).isEqualTo(0);

        assertThat(result.get(3L).teamStatus()).isEqualTo("Done");
        assertThat(result.get(3L).storyPoints()).isEqualTo(0);
    }
}
