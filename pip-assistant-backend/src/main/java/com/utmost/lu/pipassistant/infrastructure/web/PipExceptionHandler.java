package com.utmost.lu.pipassistant.infrastructure.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.utmost.lu.pipassistant.application.DuplicatePipCodeException;

/**
 * Maps PIP-related errors to HTTP responses: duplicate code → 409, invalid code → 400.
 */
@RestControllerAdvice(assignableTypes = PipController.class)
public class PipExceptionHandler {

    @ExceptionHandler(DuplicatePipCodeException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(DuplicatePipCodeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalid(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
