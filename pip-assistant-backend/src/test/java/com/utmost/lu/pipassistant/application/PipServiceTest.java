package com.utmost.lu.pipassistant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.PipCode;
import com.utmost.lu.pipassistant.domain.port.PipRepository;

class PipServiceTest {

    // Fixed clock at 2026-06-20 → current 2-digit year = 26.
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-20T00:00:00Z"), ZoneOffset.UTC);
    private final InMemoryPipRepository repository = new InMemoryPipRepository();
    private final PipService service = new PipService(repository, clock);

    @Test
    void suggestNextCode_onEmptyDb_usesCurrentYear() {
        assertThat(service.suggestNextCode()).isEqualTo("26_PIP_1");
    }

    @Test
    void suggestNextCode_incrementsGreatestSequenceNumerically() {
        service.create("26_PIP_4");
        service.create("26_PIP_9");
        service.create("26_PIP_10"); // numeric max, must beat _9

        assertThat(service.suggestNextCode()).isEqualTo("26_PIP_11");
    }

    @Test
    void list_isSortedDescending() {
        service.create("26_PIP_1");
        service.create("26_PIP_10");
        service.create("26_PIP_2");

        assertThat(service.list(null).stream().map(p -> p.code().value()).toList())
                .containsExactly("26_PIP_10", "26_PIP_2", "26_PIP_1");
    }

    @Test
    void create_rejectsDuplicate() {
        service.create("26_PIP_1");

        assertThatExceptionOfType(DuplicatePipCodeException.class)
                .isThrownBy(() -> service.create("26_PIP_1"));
    }

    @Test
    void create_rejectsInvalidFormat() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> service.create("PIP-1"));
    }

    /** Minimal in-memory port implementation for unit testing the service. */
    private static final class InMemoryPipRepository implements PipRepository {
        private final List<Pip> store = new ArrayList<>();
        private long sequence = 0;

        @Override
        public List<Pip> findAll() {
            return new ArrayList<>(store);
        }

        @Override
        public List<Pip> findByYear(int twoDigitYear) {
            return store.stream().filter(p -> p.code().year() == twoDigitYear).toList();
        }

        @Override
        public boolean existsByCode(PipCode code) {
            return store.stream().anyMatch(p -> p.code().equals(code));
        }

        @Override
        public Optional<Pip> findMax() {
            return store.stream().max(Comparator.comparing(Pip::code));
        }

        @Override
        public Pip save(Pip pip) {
            Pip persisted = new Pip(++sequence, pip.code(), pip.startDate(), pip.endDate(), pip.status());
            store.add(persisted);
            return persisted;
        }
    }
}
