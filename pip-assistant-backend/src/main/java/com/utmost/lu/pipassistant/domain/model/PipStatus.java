package com.utmost.lu.pipassistant.domain.model;

/**
 * Lifecycle of a PIP. New PIPs default to {@link #PREPARATION}; the other states are
 * set later (status editing is a future feature).
 */
public enum PipStatus {
    /** Preparation week of the cycle (default at creation). */
    PREPARATION,
    /** The 6 development weeks are running. */
    ACTIVE,
    /** The PIP is finished. */
    CLOSED
}
