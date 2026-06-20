package com.utmost.lu.pipassistant.application;

/** Thrown when a PIP id does not exist. Mapped to HTTP 404. */
public class PipNotFoundException extends RuntimeException {

    public PipNotFoundException(Long pipId) {
        super("PIP " + pipId + " not found");
    }
}
