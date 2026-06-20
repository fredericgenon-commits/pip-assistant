package com.utmost.lu.pipassistant.infrastructure.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.utmost.lu.pipassistant.application.InvalidRequirementStatusException;
import com.utmost.lu.pipassistant.application.PipNotFoundException;

/** Maps PIP-detail errors: missing PIP → 404, invalid requirement status → 400. */
@RestControllerAdvice(assignableTypes = PipDetailController.class)
public class PipDetailExceptionHandler {

    @ExceptionHandler(PipNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(PipNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidRequirementStatusException.class)
    public ResponseEntity<Map<String, String>> handleInvalidStatus(InvalidRequirementStatusException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
