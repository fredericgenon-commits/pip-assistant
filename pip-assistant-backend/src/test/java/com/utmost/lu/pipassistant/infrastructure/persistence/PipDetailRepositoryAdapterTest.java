package com.utmost.lu.pipassistant.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.utmost.lu.pipassistant.domain.model.PipStatus;
import com.utmost.lu.pipassistant.domain.model.Requirement;

@DataJpaTest
@Import(PipDetailRepositoryAdapter.class)
class PipDetailRepositoryAdapterTest {

    @Autowired
    private PipDetailRepositoryAdapter adapter;

    @Autowired
    private PipJpaRepository pipJpaRepository;

    private Long newPipId() {
        return pipJpaRepository.save(
                new PipEntity(null, "26_PIP_1", null, null, PipStatus.PREPARATION)).getId();
    }

    @Test
    void createsRequirementAndReloadsAggregate() {
        Long pipId = newPipId();

        Requirement req = adapter.createRequirement(pipId, "TCM-1", "tcm desc",
                "REQ-1", "req desc", "TODO", "pm");

        assertThat(adapter.findProjectsByPip(pipId)).singleElement()
                .extracting(p -> p.tcmKey()).isEqualTo("TCM-1");
        assertThat(adapter.findRequirementsByPip(pipId)).singleElement()
                .extracting(r -> r.reqKey()).isEqualTo("REQ-1");

        // upsert workload then update it (team 1 = Core, seeded by Flyway V3)
        adapter.upsertWorkload(req.id(), 1L, new BigDecimal("3.0"), false);
        adapter.upsertWorkload(req.id(), 1L, new BigDecimal("5.0"), false);
        assertThat(adapter.findWorkloadsByPip(pipId)).singleElement()
                .extracting(w -> w.estimate(), org.assertj.core.api.Assertions.as(
                        org.assertj.core.api.InstanceOfAssertFactories.BIG_DECIMAL))
                .isEqualByComparingTo("5.0");

        // marking the cell TBD clears the estimate and sets the flag
        adapter.upsertWorkload(req.id(), 1L, null, true);
        assertThat(adapter.findWorkloadsByPip(pipId)).singleElement()
                .satisfies(w -> {
                    assertThat(w.tbd()).isTrue();
                    assertThat(w.estimate()).isNull();
                });

        adapter.upsertDevComment(req.id(), 1L, "note");
        assertThat(adapter.findDevCommentsByPip(pipId)).singleElement()
                .extracting(c -> c.text()).isEqualTo("note");

        adapter.upsertCapacity(pipId, 1L, new BigDecimal("20.0"));
        assertThat(adapter.findCapacitiesByPip(pipId)).singleElement()
                .extracting(c -> c.capacity(), org.assertj.core.api.Assertions.as(
                        org.assertj.core.api.InstanceOfAssertFactories.BIG_DECIMAL))
                .isEqualByComparingTo("20.0");
    }

    @Test
    void updatesRequirementAndProjectDescription() {
        Long pipId = newPipId();
        Requirement req = adapter.createRequirement(pipId, "TCM-2", "old tcm",
                "REQ-2", "old req", "TODO", "pm");

        adapter.updateRequirement(req.id(), "new req", "DONE", "pm2");
        adapter.updateProjectDescription(req.projectId(), "new tcm");

        assertThat(adapter.findRequirementsByPip(pipId)).singleElement()
                .satisfies(r -> {
                    assertThat(r.description()).isEqualTo("new req");
                    assertThat(r.status()).isEqualTo("DONE");
                });
        assertThat(adapter.findProjectsByPip(pipId)).singleElement()
                .extracting(p -> p.description()).isEqualTo("new tcm");
    }
}
