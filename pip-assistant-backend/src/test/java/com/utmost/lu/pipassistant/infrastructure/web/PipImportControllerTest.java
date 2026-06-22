package com.utmost.lu.pipassistant.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utmost.lu.pipassistant.application.ImportExcelService;
import com.utmost.lu.pipassistant.application.InvalidExcelFileException;
import com.utmost.lu.pipassistant.application.PipDetailView;
import com.utmost.lu.pipassistant.application.PipNotFoundException;
import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.PipCode;
import com.utmost.lu.pipassistant.domain.model.PipStatus;
import com.utmost.lu.pipassistant.domain.model.Team;

@WebMvcTest(PipImportController.class)
class PipImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImportExcelService importExcelService;

    private static PipDetailView view() {
        Pip pip = new Pip(1L, PipCode.of("26_PIP_1"), null, null, PipStatus.PREPARATION);
        var row = new PipDetailView.RequirementRow(7L, 5L, "TCM-1", "tcm", "REQ-1",
                "req", "TODO", "pm", 1, "NEW", Map.of(10L, "3"), Map.of());
        return new PipDetailView(pip, List.of(new Team(10L, "Core")), List.of(row), Map.of());
    }

    private static MockMultipartFile file() {
        return new MockMultipartFile("file", "plan.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{1, 2, 3});
    }

    @Test
    void importReturnsRefreshedDetail() throws Exception {
        given(importExcelService.importFile(eq(1L), any(), any())).willReturn(view());

        mockMvc.perform(multipart("/api/pips/1/imports").file(file()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requirements[0].priority").value(1))
                .andExpect(jsonPath("$.requirements[0].pipStatus").value("NEW"));
    }

    @Test
    void invalidFileReturns422() throws Exception {
        given(importExcelService.importFile(eq(1L), any(), any()))
                .willThrow(new InvalidExcelFileException("bad file"));

        mockMvc.perform(multipart("/api/pips/1/imports").file(file()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void missingPipReturns404() throws Exception {
        given(importExcelService.importFile(eq(9L), any(), any()))
                .willThrow(new PipNotFoundException(9L));

        mockMvc.perform(multipart("/api/pips/9/imports").file(file()))
                .andExpect(status().isNotFound());
    }
}
