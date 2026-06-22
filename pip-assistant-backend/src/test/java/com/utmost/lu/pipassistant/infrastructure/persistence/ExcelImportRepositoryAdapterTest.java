package com.utmost.lu.pipassistant.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.utmost.lu.pipassistant.domain.model.DiffedRequirement;
import com.utmost.lu.pipassistant.domain.model.ImportDiff;
import com.utmost.lu.pipassistant.domain.model.ParsedRequirement;
import com.utmost.lu.pipassistant.domain.model.PipStatus;
import com.utmost.lu.pipassistant.domain.model.RequirementPipStatus;
import com.utmost.lu.pipassistant.domain.model.Requirement;
import com.utmost.lu.pipassistant.domain.model.SnapshotRequirement;

@DataJpaTest
@Import({ExcelImportRepositoryAdapter.class, PipDetailRepositoryAdapter.class})
class ExcelImportRepositoryAdapterTest {

    @Autowired
    private ExcelImportRepositoryAdapter importAdapter;

    @Autowired
    private PipDetailRepositoryAdapter detailAdapter;

    @Autowired
    private PipJpaRepository pipJpaRepository;

    private static final long CORE_TEAM_ID = 1L; // seeded first by Flyway V3

    private Long newPipId() {
        return pipJpaRepository.save(
                new PipEntity(null, "26_PIP_1", null, null, PipStatus.PREPARATION)).getId();
    }

    private static DiffedRequirement diffed(int order, String reqKey, Map<String, BigDecimal> wl,
                                            int priority, RequirementPipStatus status) {
        ParsedRequirement parsed = new ParsedRequirement(order, "TCM-1", "Project A",
                reqKey, "desc", "comment", wl, false);
        return new DiffedRequirement(parsed, priority, status);
    }

    @Test
    void firstImport_storesVersionSnapshotAndLiveRows() {
        Long pipId = newPipId();
        assertThat(importAdapter.nextVersionNo(pipId)).isEqualTo(1);

        ImportDiff diff = new ImportDiff(List.of(
                diffed(0, "REQ-10", Map.of("Core", new BigDecimal("5")), 1, RequirementPipStatus.NEW)),
                List.of());
        importAdapter.applyImport(pipId, 1, "v1.xlsx", Instant.now(), diff);

        assertThat(importAdapter.nextVersionNo(pipId)).isEqualTo(2);

        List<SnapshotRequirement> snapshot = importAdapter.findLatestSnapshot(pipId);
        assertThat(snapshot).singleElement().satisfies(s -> {
            assertThat(s.reqKey()).isEqualTo("REQ-10");
            assertThat(s.workloadsByTeam().get("Core")).isEqualByComparingTo("5");
        });

        Requirement live = detailAdapter.findRequirementsByPip(pipId).get(0);
        assertThat(live.priority()).isEqualTo(1);
        assertThat(live.pipStatus()).isEqualTo("NEW");
        assertThat(detailAdapter.findWorkloadsByPip(pipId)).singleElement()
                .extracting(w -> w.estimate(), org.assertj.core.api.Assertions.as(
                        org.assertj.core.api.InstanceOfAssertFactories.BIG_DECIMAL))
                .isEqualByComparingTo("5");
    }

    @Test
    void reimport_keepsManuallyEditedWorkload() {
        Long pipId = newPipId();
        importAdapter.applyImport(pipId, 1, "v1.xlsx", Instant.now(), new ImportDiff(List.of(
                diffed(0, "REQ-10", Map.of("Core", new BigDecimal("5")), 1, RequirementPipStatus.NEW)),
                List.of()));

        Requirement live = detailAdapter.findRequirementsByPip(pipId).get(0);
        // User edits the Core workload by hand (5 -> 8): this marks a manual override.
        detailAdapter.upsertWorkload(live.id(), CORE_TEAM_ID, new BigDecimal("8"), false);

        // v2 file says Core = 3, but the manual value must survive.
        importAdapter.applyImport(pipId, 2, "v2.xlsx", Instant.now(), new ImportDiff(List.of(
                diffed(0, "REQ-10", Map.of("Core", new BigDecimal("3")), 1, RequirementPipStatus.CHANGED)),
                List.of()));

        assertThat(detailAdapter.findWorkloadsByPip(pipId)).singleElement()
                .extracting(w -> w.estimate(), org.assertj.core.api.Assertions.as(
                        org.assertj.core.api.InstanceOfAssertFactories.BIG_DECIMAL))
                .isEqualByComparingTo("8");
        // The raw v2 snapshot still records the file value (3).
        assertThat(importAdapter.findLatestSnapshot(pipId)).singleElement()
                .satisfies(s -> assertThat(s.workloadsByTeam().get("Core")).isEqualByComparingTo("3"));
    }

    @Test
    void reimport_marksRemovedRequirement() {
        Long pipId = newPipId();
        importAdapter.applyImport(pipId, 1, "v1.xlsx", Instant.now(), new ImportDiff(List.of(
                diffed(0, "REQ-10", Map.of(), 1, RequirementPipStatus.NEW),
                diffed(1, "REQ-11", Map.of(), 2, RequirementPipStatus.NEW)),
                List.of()));

        // v2: REQ-11 disappeared from the file.
        importAdapter.applyImport(pipId, 2, "v2.xlsx", Instant.now(), new ImportDiff(List.of(
                diffed(0, "REQ-10", Map.of(), 1, RequirementPipStatus.UNCHANGED)),
                List.of("REQ-11")));

        Requirement removed = detailAdapter.findRequirementsByPip(pipId).stream()
                .filter(r -> r.reqKey().equals("REQ-11")).findFirst().orElseThrow();
        assertThat(removed.pipStatus()).isEqualTo("REMOVED_FROM_PIP");
        assertThat(removed.priority()).isNull();
    }
}
