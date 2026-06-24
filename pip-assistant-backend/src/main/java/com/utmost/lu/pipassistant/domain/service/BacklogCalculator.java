package com.utmost.lu.pipassistant.domain.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.utmost.lu.pipassistant.domain.model.DevTicket;

/**
 * Pure domain service: computes per-team backlog story points and Team Status from a list of
 * JIRA dev tickets belonging to one REQ.
 *
 * <p>No I/O, no Spring dependencies — fully unit-testable.
 */
public class BacklogCalculator {

    private static final List<String> TA_PREFIXES = List.of(
            "[technical analysis]",
            "[technical_analysis]",
            "[technical-analysis]",
            "[tech. analysis]");

    private static final String STATUS_OPEN = "open";
    private static final String STATUS_TBE  = "to be estimated";
    private static final String STATUS_RFI  = "ready for implementation";
    private static final String STATUS_ABD  = "abandonned";
    private static final String STATUS_IN_PROGRESS = "in progress";

    public record TeamBacklogResult(Long teamId, int storyPoints, String teamStatus) {}

    /**
     * Computes backlog results for each team that has at least one ticket mapped to it.
     * Teams with no tickets produce no entry in the result map.
     *
     * @param tickets      tickets fetched from JIRA for one REQ
     * @param jiraTeamToId mapping from the raw JIRA team field value to the system team id
     */
    public Map<Long, TeamBacklogResult> compute(List<DevTicket> tickets, Map<String, Long> jiraTeamToId) {
        // Group tickets by mapped team id; ignore tickets with unmapped teams.
        Map<Long, List<DevTicket>> byTeam = new HashMap<>();
        for (DevTicket t : tickets) {
            if (!"project".equalsIgnoreCase(trim(t.deliveryMethod()))) {
                continue;
            }
            Long teamId = jiraTeamToId.get(t.jiraTeam());
            if (teamId == null) {
                continue;
            }
            byTeam.computeIfAbsent(teamId, k -> new java.util.ArrayList<>()).add(t);
        }

        Map<Long, TeamBacklogResult> results = new HashMap<>();
        for (Map.Entry<Long, List<DevTicket>> entry : byTeam.entrySet()) {
            Long teamId = entry.getKey();
            List<DevTicket> teamTickets = entry.getValue();
            int sp = computeBacklogSp(teamTickets);
            String status = computeTeamStatus(teamTickets);
            results.put(teamId, new TeamBacklogResult(teamId, sp, status));
        }
        return results;
    }

    private int computeBacklogSp(List<DevTicket> tickets) {
        return tickets.stream()
                .filter(t -> !isTa(t))
                .filter(t -> !isAbandonned(t))
                .filter(t -> STATUS_RFI.equalsIgnoreCase(trim(t.status())))
                .mapToInt(t -> t.storyPoints() != null ? t.storyPoints() : 0)
                .sum();
    }

    private String computeTeamStatus(List<DevTicket> tickets) {
        List<DevTicket> taActive = tickets.stream()
                .filter(this::isTa)
                .filter(t -> !isAbandonned(t))
                .toList();

        // Priority 1: any non-abandoned TA in open / tbe / rfi
        boolean taTodo = taActive.stream()
                .anyMatch(t -> isOpenLike(t.status()));
        if (taTodo) {
            return "TA todo";
        }

        // Priority 2: any TA in progress
        boolean taOngoing = taActive.stream()
                .anyMatch(t -> STATUS_IN_PROGRESS.equalsIgnoreCase(trim(t.status())));
        if (taOngoing) {
            return "TA ongoing";
        }

        List<DevTicket> devActive = tickets.stream()
                .filter(t -> !isTa(t))
                .filter(t -> !isAbandonned(t))
                .toList();

        if (devActive.isEmpty()) {
            return null;
        }

        // Priority 3: any DEV in tbe
        boolean anyTbe = devActive.stream()
                .anyMatch(t -> STATUS_TBE.equalsIgnoreCase(trim(t.status())));
        if (anyTbe) {
            return "To be estimated";
        }

        // Priority 4: all DEV in rfi
        boolean allRfi = devActive.stream()
                .allMatch(t -> STATUS_RFI.equalsIgnoreCase(trim(t.status())));
        if (allRfi) {
            return "Ready";
        }

        // Priority 5: all DEV done (not open, tbe, rfi, abandonned)
        boolean allDone = devActive.stream().allMatch(this::isDone);
        if (allDone) {
            return "Done";
        }

        return null;
    }

    private boolean isTa(DevTicket t) {
        String lc = t.summary() == null ? "" : t.summary().toLowerCase(Locale.ROOT).stripLeading();
        return TA_PREFIXES.stream().anyMatch(lc::startsWith);
    }

    private boolean isAbandonned(DevTicket t) {
        return STATUS_ABD.equalsIgnoreCase(trim(t.status()));
    }

    private boolean isOpenLike(String status) {
        String s = trim(status);
        return STATUS_OPEN.equalsIgnoreCase(s)
                || STATUS_TBE.equalsIgnoreCase(s)
                || STATUS_RFI.equalsIgnoreCase(s);
    }

    private boolean isDone(DevTicket t) {
        String s = trim(t.status());
        return !STATUS_OPEN.equalsIgnoreCase(s)
                && !STATUS_TBE.equalsIgnoreCase(s)
                && !STATUS_RFI.equalsIgnoreCase(s)
                && !STATUS_ABD.equalsIgnoreCase(s);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
