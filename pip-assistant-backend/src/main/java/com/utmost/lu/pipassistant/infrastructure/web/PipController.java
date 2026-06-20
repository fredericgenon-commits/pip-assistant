package com.utmost.lu.pipassistant.infrastructure.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.utmost.lu.pipassistant.application.PipService;
import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.infrastructure.web.dto.CreatePipRequest;
import com.utmost.lu.pipassistant.infrastructure.web.dto.NextCodeResponse;
import com.utmost.lu.pipassistant.infrastructure.web.dto.PipResponse;

/**
 * REST API for PIPs.
 */
@RestController
@RequestMapping("/api/pips")
public class PipController {

    private final PipService pipService;

    public PipController(PipService pipService) {
        this.pipService = pipService;
    }

    /** List PIPs (sorted descending), optionally filtered by 2-digit year. */
    @GetMapping
    public List<PipResponse> list(@RequestParam(name = "year", required = false) Integer year) {
        return pipService.list(year).stream().map(PipResponse::from).toList();
    }

    /** Distinct 2-digit years present, descending (for the year filter). */
    @GetMapping("/years")
    public List<Integer> years() {
        return pipService.distinctYears();
    }

    /** Suggested code for a new PIP. */
    @GetMapping("/next-code")
    public NextCodeResponse nextCode() {
        return new NextCodeResponse(pipService.suggestNextCode());
    }

    /** Create a PIP. */
    @PostMapping
    public ResponseEntity<PipResponse> create(@RequestBody CreatePipRequest request) {
        Pip created = pipService.create(request.code());
        PipResponse body = PipResponse.from(created);
        return ResponseEntity.created(URI.create("/api/pips/" + created.id())).body(body);
    }
}
