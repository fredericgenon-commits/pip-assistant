package com.utmost.lu.pipassistant.application;

import java.util.List;

/** Thrown when a requirement status is not in the configured list. Mapped to HTTP 400. */
public class InvalidRequirementStatusException extends RuntimeException {

    public InvalidRequirementStatusException(String status, List<String> allowed) {
        super("Invalid requirement status '" + status + "', allowed values: " + allowed);
    }
}
