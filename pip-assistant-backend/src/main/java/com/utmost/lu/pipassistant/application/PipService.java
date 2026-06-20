package com.utmost.lu.pipassistant.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.PipCode;
import com.utmost.lu.pipassistant.domain.port.PipRepository;

/**
 * Application service for PIPs: listing, distinct years, next-code suggestion and creation.
 */
@Service
public class PipService {

    private final PipRepository pipRepository;
    private final Clock clock;

    public PipService(PipRepository pipRepository, Clock clock) {
        this.pipRepository = pipRepository;
        this.clock = clock;
    }

    /** PIPs sorted by code descending (year then sequence). Optional 2-digit year filter. */
    @Transactional(readOnly = true)
    public List<Pip> list(Integer twoDigitYear) {
        List<Pip> pips = (twoDigitYear == null)
                ? pipRepository.findAll()
                : pipRepository.findByYear(twoDigitYear);
        return pips.stream()
                .sorted(Comparator.comparing(Pip::code).reversed())
                .toList();
    }

    /** Distinct 2-digit years present, descending. */
    @Transactional(readOnly = true)
    public List<Integer> distinctYears() {
        return pipRepository.findAll().stream()
                .map(pip -> pip.code().year())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    /**
     * Suggested code for a new PIP: the greatest existing code with its sequence
     * incremented; or {@code <currentYY>_PIP_1} when there is no PIP yet.
     */
    @Transactional(readOnly = true)
    public String suggestNextCode() {
        Optional<Pip> max = pipRepository.findMax();
        if (max.isPresent()) {
            return max.get().code().nextSequence().value();
        }
        int currentTwoDigitYear = LocalDate.now(clock).getYear() % 100;
        return PipCode.firstForYear(currentTwoDigitYear).value();
    }

    /**
     * Creates a PIP from a raw code.
     *
     * @throws IllegalArgumentException     if the code does not match {@code yy_PIP_n}.
     * @throws DuplicatePipCodeException    if the code already exists.
     */
    @Transactional
    public Pip create(String rawCode) {
        PipCode code = PipCode.of(rawCode);
        if (pipRepository.existsByCode(code)) {
            throw new DuplicatePipCodeException(code.value());
        }
        return pipRepository.save(Pip.newPip(code));
    }
}
