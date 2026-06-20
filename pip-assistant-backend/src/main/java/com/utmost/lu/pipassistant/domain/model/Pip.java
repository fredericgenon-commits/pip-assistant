package com.utmost.lu.pipassistant.domain.model;

import java.time.LocalDate;

/**
 * A PIP (a 7-week planning cycle). Identified by its {@link PipCode}. Dates are not yet
 * captured by the UI (nullable for now); {@link #status} defaults to
 * {@link PipStatus#PREPARATION} at creation.
 */
public final class Pip {

    private final Long id;
    private final PipCode code;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final PipStatus status;

    public Pip(Long id, PipCode code, LocalDate startDate, LocalDate endDate, PipStatus status) {
        this.id = id;
        this.code = code;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    /** Creates a brand-new PIP (no id yet) with the default status and no dates. */
    public static Pip newPip(PipCode code) {
        return new Pip(null, code, null, null, PipStatus.PREPARATION);
    }

    public Long id() {
        return id;
    }

    public PipCode code() {
        return code;
    }

    public LocalDate startDate() {
        return startDate;
    }

    public LocalDate endDate() {
        return endDate;
    }

    public PipStatus status() {
        return status;
    }
}
