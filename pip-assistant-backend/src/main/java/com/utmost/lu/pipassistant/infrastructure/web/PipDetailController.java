package com.utmost.lu.pipassistant.infrastructure.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utmost.lu.pipassistant.application.BacklogPatch;
import com.utmost.lu.pipassistant.application.PipDetailService;
import com.utmost.lu.pipassistant.domain.model.Requirement;
import com.utmost.lu.pipassistant.infrastructure.config.JiraProperties;
import com.utmost.lu.pipassistant.infrastructure.web.dto.CreateRequirementRequest;
import com.utmost.lu.pipassistant.infrastructure.web.dto.PipDetailResponse;
import com.utmost.lu.pipassistant.infrastructure.web.dto.SavePipDetailRequest;

/** REST API for the PIP Details screen. */
@RestController
@RequestMapping("/api")
public class PipDetailController {

    private final PipDetailService pipDetailService;
    private final JiraProperties jiraProperties;

    public PipDetailController(PipDetailService pipDetailService, JiraProperties jiraProperties) {
        this.pipDetailService = pipDetailService;
        this.jiraProperties = jiraProperties;
    }

    /** Aggregated detail of a PIP. */
    @GetMapping("/pips/{id}/detail")
    public PipDetailResponse getDetail(@PathVariable("id") Long id) {
        return PipDetailResponse.from(pipDetailService.getDetail(id), jiraProperties.getBaseUrl());
    }

    /** JIRA-derived fields only, used by the frontend to patch the table in place after a sync. */
    @GetMapping("/pips/{id}/backlog")
    public List<BacklogPatch> getBacklog(@PathVariable("id") Long id) {
        return pipDetailService.getBacklog(id);
    }

    /** Configurable list of requirement statuses (fills the status cell select). */
    @GetMapping("/requirement-statuses")
    public List<String> requirementStatuses() {
        return pipDetailService.requirementStatuses();
    }

    /** Bulk save of the PIP Details screen. */
    @PutMapping("/pips/{id}/detail")
    public ResponseEntity<Void> save(@PathVariable("id") Long id, @RequestBody SavePipDetailRequest request) {
        pipDetailService.save(id, request.toCommand());
        return ResponseEntity.noContent().build();
    }

    /** Interim requirement creation (tests / future Excel-JIRA import; not used by the UI). */
    @PostMapping("/pips/{id}/requirements")
    public ResponseEntity<Long> createRequirement(
            @PathVariable("id") Long id, @RequestBody CreateRequirementRequest request) {
        Requirement created = pipDetailService.createRequirement(
                id, request.tcmKey(), request.tcmDescription(), request.reqKey(),
                request.description(), request.status(), request.pmComment());
        return ResponseEntity.created(URI.create("/api/requirements/" + created.id())).body(created.id());
    }
}
