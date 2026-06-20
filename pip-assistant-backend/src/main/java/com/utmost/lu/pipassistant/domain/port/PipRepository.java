package com.utmost.lu.pipassistant.domain.port;

import java.util.List;
import java.util.Optional;

import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.PipCode;

/**
 * Outbound port for PIP persistence. Implemented by an infrastructure adapter.
 */
public interface PipRepository {

    /** All PIPs, unordered (ordering is applied by the application layer). */
    List<Pip> findAll();

    /** PIPs whose code belongs to the given 2-digit year. */
    List<Pip> findByYear(int twoDigitYear);

    boolean existsByCode(PipCode code);

    /** The greatest PIP overall (by year then sequence), if any. */
    Optional<Pip> findMax();

    Optional<Pip> findById(Long id);

    Pip save(Pip pip);
}
