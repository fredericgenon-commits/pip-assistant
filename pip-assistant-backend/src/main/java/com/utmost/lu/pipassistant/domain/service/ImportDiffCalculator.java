package com.utmost.lu.pipassistant.domain.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.utmost.lu.pipassistant.domain.model.DiffedRequirement;
import com.utmost.lu.pipassistant.domain.model.ImportDiff;
import com.utmost.lu.pipassistant.domain.model.ParsedRequirement;
import com.utmost.lu.pipassistant.domain.model.RequirementPipStatus;
import com.utmost.lu.pipassistant.domain.model.SnapshotRequirement;

/**
 * Pure diff logic for an Excel import: assigns each parsed REQ a 1-based priority (file
 * order) and a {@link RequirementPipStatus} by comparing it, by REQ key, to the previous
 * version's snapshot. No I/O, fully unit-testable.
 */
public class ImportDiffCalculator {

    /**
     * @param current  parsed requirements in file order (priority = index + 1)
     * @param previous requirements of the previous version (empty for the first import)
     */
    public ImportDiff diff(List<ParsedRequirement> current, List<SnapshotRequirement> previous) {
        Map<String, SnapshotRequirement> previousByKey = previous.stream()
                .collect(Collectors.toMap(SnapshotRequirement::reqKey, Function.identity(),
                        (a, b) -> b, LinkedHashMap::new));

        List<DiffedRequirement> diffed = new ArrayList<>(current.size());
        Set<String> seenKeys = new java.util.HashSet<>();
        int priority = 0;
        for (ParsedRequirement parsed : current) {
            priority++;
            seenKeys.add(parsed.reqKey());
            RequirementPipStatus status =
                    statusOf(parsed, priority, previousByKey.get(parsed.reqKey()));
            diffed.add(new DiffedRequirement(parsed, priority, status));
        }

        List<String> removed = previousByKey.keySet().stream()
                .filter(key -> !seenKeys.contains(key))
                .toList();

        return new ImportDiff(diffed, removed);
    }

    private RequirementPipStatus statusOf(ParsedRequirement parsed, int priority, SnapshotRequirement before) {
        if (parsed.missingData()) {
            return RequirementPipStatus.MISSING_DATA;
        }
        if (before == null) {
            return RequirementPipStatus.NEW;
        }
        if (!contentEquals(parsed, before)) {
            return RequirementPipStatus.CHANGED;
        }
        boolean samePriority = before.priority() != null && before.priority() == priority;
        return samePriority ? RequirementPipStatus.UNCHANGED : RequirementPipStatus.PRIORITY_CHANGED;
    }

    private boolean contentEquals(ParsedRequirement parsed, SnapshotRequirement before) {
        return Objects.equals(blankToNull(parsed.tcmKey()), blankToNull(before.tcmKey()))
                && Objects.equals(blankToNull(parsed.tcmDescription()), blankToNull(before.tcmDescription()))
                && Objects.equals(blankToNull(parsed.reqDescription()), blankToNull(before.reqDescription()))
                && Objects.equals(blankToNull(parsed.pmComment()), blankToNull(before.pmComment()))
                && workloadsEqual(parsed.workloadsByTeam(), before.workloadsByTeam());
    }

    /** Compare per-team workloads numerically (5 equals 5.00); absent equals zero/absent. */
    private boolean workloadsEqual(Map<String, BigDecimal> a, Map<String, BigDecimal> b) {
        Set<String> teams = new java.util.HashSet<>();
        teams.addAll(a.keySet());
        teams.addAll(b.keySet());
        for (String team : teams) {
            BigDecimal va = a.get(team);
            BigDecimal vb = b.get(team);
            if (va == null && vb == null) {
                continue;
            }
            if (va == null || vb == null || va.compareTo(vb) != 0) {
                return false;
            }
        }
        return true;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
