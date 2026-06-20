package com.utmost.lu.pipassistant.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.PipCode;

@DataJpaTest
@Import(PipRepositoryAdapter.class)
class PipRepositoryAdapterTest {

    @Autowired
    private PipRepositoryAdapter adapter;

    @Test
    void savesAndReadsBackByYear() {
        adapter.save(Pip.newPip(PipCode.of("26_PIP_1")));
        adapter.save(Pip.newPip(PipCode.of("25_PIP_3")));

        assertThat(adapter.findByYear(26)).extracting(p -> p.code().value())
                .containsExactly("26_PIP_1");
        assertThat(adapter.findAll()).hasSize(2);
    }

    @Test
    void existsByCodeAndFindMax() {
        adapter.save(Pip.newPip(PipCode.of("26_PIP_2")));
        adapter.save(Pip.newPip(PipCode.of("26_PIP_10")));

        assertThat(adapter.existsByCode(PipCode.of("26_PIP_2"))).isTrue();
        assertThat(adapter.existsByCode(PipCode.of("26_PIP_99"))).isFalse();
        assertThat(adapter.findMax()).get().extracting(p -> p.code().value()).isEqualTo("26_PIP_10");
    }
}
