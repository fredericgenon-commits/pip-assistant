package com.utmost.lu.pipassistant.application;

/**
 * Thrown when creating a PIP whose code already exists. Mapped to HTTP 409 by the web layer.
 */
public class DuplicatePipCodeException extends RuntimeException {

    public DuplicatePipCodeException(String code) {
        super("A PIP with code '" + code + "' already exists");
    }
}
