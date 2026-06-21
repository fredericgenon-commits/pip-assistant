package com.utmost.lu.pipassistant.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utmost.lu.pipassistant.application.InvalidRequirementStatusException;
import com.utmost.lu.pipassistant.application.PipDetailService;
import com.utmost.lu.pipassistant.application.PipDetailView;
import com.utmost.lu.pipassistant.application.PipNotFoundException;
import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.PipCode;
import com.utmost.lu.pipassistant.domain.model.PipStatus;
import com.utmost.lu.pipassistant.domain.model.Team;

@WebMvcTest(PipDetailController.class)
class PipDetailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PipDetailService pipDetailService;

    private static PipDetailView view() {
        Pip pip = new Pip(1L, PipCode.of("26_PIP_1"), null, null, PipStatus.PREPARATION);
        var row = new PipDetailView.RequirementRow(7L, 5L, "TCM-1", "tcm desc", "REQ-1",
                "req desc", "TODO", "pm", 1, "NEW", Map.of(10L, new BigDecimal("3")), Map.of(10L, "note"));
        return new PipDetailView(pip, List.of(new Team(10L, "Core")), List.of(row),
                Map.of(10L, new BigDecimal("20")));
    }

    @Test
    void getDetailReturnsAggregate() throws Exception {
        given(pipDetailService.getDetail(1L)).willReturn(view());

        mockMvc.perform(get("/api/pips/1/detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pip.code").value("26_PIP_1"))
                .andExpect(jsonPath("$.teams[0].name").value("Core"))
                .andExpect(jsonPath("$.requirements[0].tcmKey").value("TCM-1"))
                .andExpect(jsonPath("$.requirements[0].workloads.10").value(3))
                .andExpect(jsonPath("$.capacities.10").value(20));
    }

    @Test
    void getDetailMissingReturns404() throws Exception {
        given(pipDetailService.getDetail(99L)).willThrow(new PipNotFoundException(99L));

        mockMvc.perform(get("/api/pips/99/detail")).andExpect(status().isNotFound());
    }

    @Test
    void saveReturns204() throws Exception {
        mockMvc.perform(put("/api/pips/1/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requirements\":[],\"capacities\":{}}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void saveInvalidStatusReturns400() throws Exception {
        doThrow(new InvalidRequirementStatusException("WRONG", List.of("TODO")))
                .when(pipDetailService).save(eq(1L), any());

        mockMvc.perform(put("/api/pips/1/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requirements\":[{\"id\":7,\"status\":\"WRONG\"}],\"capacities\":{}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requirementStatusesReturnsConfiguredList() throws Exception {
        given(pipDetailService.requirementStatuses()).willReturn(List.of("TODO", "IN_PROGRESS", "DONE"));

        mockMvc.perform(get("/api/requirement-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1]").value("IN_PROGRESS"));
    }
}
