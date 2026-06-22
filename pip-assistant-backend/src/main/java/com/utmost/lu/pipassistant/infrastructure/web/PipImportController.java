package com.utmost.lu.pipassistant.infrastructure.web;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.utmost.lu.pipassistant.application.ImportExcelService;
import com.utmost.lu.pipassistant.application.InvalidExcelFileException;
import com.utmost.lu.pipassistant.application.JiraSyncService;
import com.utmost.lu.pipassistant.application.PipDetailService;
import com.utmost.lu.pipassistant.infrastructure.config.JiraProperties;
import com.utmost.lu.pipassistant.infrastructure.web.dto.PipDetailResponse;

/** REST API for the Excel import on the PIP Details screen. */
@RestController
@RequestMapping("/api")
public class PipImportController {

    private final ImportExcelService importExcelService;
    private final JiraSyncService jiraSyncService;
    private final PipDetailService pipDetailService;
    private final JiraProperties jiraProperties;

    public PipImportController(ImportExcelService importExcelService,
                               JiraSyncService jiraSyncService,
                               PipDetailService pipDetailService,
                               JiraProperties jiraProperties) {
        this.importExcelService = importExcelService;
        this.jiraSyncService = jiraSyncService;
        this.pipDetailService = pipDetailService;
        this.jiraProperties = jiraProperties;
    }

    /**
     * Import a PM planning file into the PIP. After parsing and versioning the Excel,
     * the JIRA status is synchronised for all requirements; returns the refreshed detail.
     */
    @PostMapping("/pips/{id}/imports")
    public PipDetailResponse importExcel(@PathVariable("id") Long id,
                                         @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidExcelFileException("The uploaded file is empty.");
        }
        try (InputStream content = file.getInputStream()) {
            importExcelService.importFile(id, file.getOriginalFilename(), content);
        } catch (IOException e) {
            throw new InvalidExcelFileException("Unable to read the uploaded file.", e);
        }
        jiraSyncService.sync(id);
        return PipDetailResponse.from(pipDetailService.getDetail(id), jiraProperties.getBaseUrl());
    }
}
