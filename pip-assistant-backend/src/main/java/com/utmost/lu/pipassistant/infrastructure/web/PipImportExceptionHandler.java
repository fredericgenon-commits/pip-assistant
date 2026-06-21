package com.utmost.lu.pipassistant.infrastructure.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.utmost.lu.pipassistant.application.InvalidExcelFileException;
import com.utmost.lu.pipassistant.application.PipNotFoundException;

/** Maps import errors: missing PIP → 404, unreadable/invalid file → 422. */
@RestControllerAdvice(assignableTypes = PipImportController.class)
public class PipImportExceptionHandler {

    @ExceptionHandler(PipNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(PipNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(InvalidExcelFileException.class)
    public ResponseEntity<Map<String, String>> handleInvalidFile(InvalidExcelFileException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("message", ex.getMessage()));
    }
}
