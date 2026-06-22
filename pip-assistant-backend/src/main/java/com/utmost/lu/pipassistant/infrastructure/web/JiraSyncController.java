package com.utmost.lu.pipassistant.infrastructure.web;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utmost.lu.pipassistant.application.JiraSyncService;
import com.utmost.lu.pipassistant.infrastructure.web.dto.JiraSyncResponse;

/** REST endpoint for triggering a JIRA status synchronisation for all requirements of a PIP. */
@RestController
@RequestMapping("/api")
public class JiraSyncController {

    private final JiraSyncService jiraSyncService;

    public JiraSyncController(JiraSyncService jiraSyncService) {
        this.jiraSyncService = jiraSyncService;
    }

    @PostMapping("/pips/{id}/jira-sync")
    public JiraSyncResponse sync(@PathVariable("id") Long id) {
        return JiraSyncResponse.from(jiraSyncService.sync(id));
    }
}
