package com.utmost.lu.pipassistant.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.utmost.lu.pipassistant.domain.model.ImportDiff;
import com.utmost.lu.pipassistant.domain.model.ParsedRequirement;
import com.utmost.lu.pipassistant.domain.model.RequirementPipStatus;
import com.utmost.lu.pipassistant.domain.model.SnapshotRequirement;

class ImportDiffCalculatorTest {

    private final ImportDiffCalculator calculator = new ImportDiffCalculator();

    private static ParsedRequirement parsed(int order, String reqKey, String reqDesc,
                                            Map<String, BigDecimal> workloads, boolean missing) {
        return new ParsedRequirement(order, "TCM-1", "Project", reqKey, reqDesc, "c", workloads, missing);
    }

    private static SnapshotRequirement snapshot(String reqKey, String reqDesc, int priority,
                                                Map<String, BigDecimal> workloads) {
        return new SnapshotRequirement(reqKey, "TCM-1", "Project", reqDesc, "c", priority, workloads);
    }

    @Test
    void firstImport_allNew_withSequentialPriority() {
        ImportDiff diff = calculator.diff(List.of(
                parsed(0, "REQ-1", "a", Map.of(), false),
                parsed(1, "REQ-2", "b", Map.of(), false)), List.of());

        assertThat(diff.removedReqKeys()).isEmpty();
        assertThat(diff.current()).extracting(d -> d.priority()).containsExactly(1, 2);
        assertThat(diff.current()).allMatch(d -> d.status() == RequirementPipStatus.NEW);
    }

    @Test
    void detectsUnchanged_changed_priorityChanged_new_removed() {
        List<SnapshotRequirement> previous = List.of(
                snapshot("REQ-1", "a", 1, Map.of("Core", new BigDecimal("5"))),
                snapshot("REQ-2", "b", 2, Map.of()),
                snapshot("REQ-3", "c", 3, Map.of()));

        // REQ-1 stays at priority 1, same content -> UNCHANGED
        // REQ-3 moves from priority 3 to 2 (content same) -> PRIORITY_CHANGED
        // REQ-4 new -> NEW
        // REQ-2 absent -> REMOVED_FROM_PIP
        ImportDiff diff = calculator.diff(List.of(
                parsed(0, "REQ-1", "a", Map.of("Core", new BigDecimal("5.00")), false),
                parsed(1, "REQ-3", "c", Map.of(), false),
                parsed(2, "REQ-4", "d", Map.of(), false)), previous);

        assertThat(byKey(diff, "REQ-1").status()).isEqualTo(RequirementPipStatus.UNCHANGED);
        assertThat(byKey(diff, "REQ-3").status()).isEqualTo(RequirementPipStatus.PRIORITY_CHANGED);
        assertThat(byKey(diff, "REQ-4").status()).isEqualTo(RequirementPipStatus.NEW);
        assertThat(diff.removedReqKeys()).containsExactly("REQ-2");
    }

    @Test
    void contentChange_marksChanged() {
        List<SnapshotRequirement> previous = List.of(
                snapshot("REQ-1", "a", 1, Map.of("Core", new BigDecimal("5"))));

        ImportDiff diff = calculator.diff(List.of(
                parsed(0, "REQ-1", "a", Map.of("Core", new BigDecimal("8")), false)), previous);

        assertThat(byKey(diff, "REQ-1").status()).isEqualTo(RequirementPipStatus.CHANGED);
    }

    @Test
    void missingData_overridesOtherStatuses() {
        ImportDiff diff = calculator.diff(List.of(
                parsed(0, "REQ-1", null, Map.of(), true)), List.of());

        assertThat(byKey(diff, "REQ-1").status()).isEqualTo(RequirementPipStatus.MISSING_DATA);
    }

    private static com.utmost.lu.pipassistant.domain.model.DiffedRequirement byKey(ImportDiff diff, String key) {
        return diff.current().stream()
                .filter(d -> d.parsed().reqKey().equals(key))
                .findFirst().orElseThrow();
    }
}
