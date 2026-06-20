package com.utmost.lu.pipassistant.infrastructure.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal "hello world" health endpoint used to verify the frontend/backend
 * round-trip in Phase 1. The full Actuator health endpoint remains available at
 * {@code /actuator/health}.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
