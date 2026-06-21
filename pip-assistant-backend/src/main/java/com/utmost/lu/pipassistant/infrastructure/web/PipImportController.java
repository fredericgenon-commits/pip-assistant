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
import com.utmost.lu.pipassistant.infrastructure.web.dto.PipDetailResponse;

/** REST API for the Excel import on the PIP Details screen. */
@RestController
@RequestMapping("/api")
public class PipImportController {

    private final ImportExcelService importExcelService;

    public PipImportController(ImportExcelService importExcelService) {
        this.importExcelService = importExcelService;
    }

    /** Import a PM planning file into the PIP; returns the refreshed detail. */
    @PostMapping("/pips/{id}/imports")
    public PipDetailResponse importExcel(@PathVariable("id") Long id,
                                         @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidExcelFileException("The uploaded file is empty.");
        }
        try (InputStream content = file.getInputStream()) {
            return PipDetailResponse.from(
                    importExcelService.importFile(id, file.getOriginalFilename(), content));
        } catch (IOException e) {
            throw new InvalidExcelFileException("Unable to read the uploaded file.", e);
        }
    }
}
