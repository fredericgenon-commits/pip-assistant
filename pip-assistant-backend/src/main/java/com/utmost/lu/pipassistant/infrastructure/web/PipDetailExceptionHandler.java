package com.utmost.lu.pipassistant.infrastructure.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.utmost.lu.pipassistant.application.PipNotFoundException;

/** Maps PIP errors: missing PIP → 404. */
@RestControllerAdvice(assignableTypes = {PipDetailController.class, JiraSyncController.class})
public class PipDetailExceptionHandler {

    @ExceptionHandler(PipNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(PipNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }
}
