package com.utmost.lu.pipassistant.infrastructure.web;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.utmost.lu.pipassistant.application.DuplicatePipCodeException;
import com.utmost.lu.pipassistant.application.PipService;
import com.utmost.lu.pipassistant.domain.model.Pip;
import com.utmost.lu.pipassistant.domain.model.PipCode;
import com.utmost.lu.pipassistant.domain.model.PipStatus;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

@WebMvcTest(PipController.class)
class PipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PipService pipService;

    private static Pip pip(long id, String code) {
        return new Pip(id, PipCode.of(code), null, null, PipStatus.PREPARATION);
    }

    @Test
    void listReturnsPips() throws Exception {
        given(pipService.list(isNull())).willReturn(List.of(pip(1L, "26_PIP_1")));

        mockMvc.perform(get("/api/pips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("26_PIP_1"))
                .andExpect(jsonPath("$[0].status").value("PREPARATION"));
    }

    @Test
    void yearsReturnsDistinctYears() throws Exception {
        given(pipService.distinctYears()).willReturn(List.of(26));

        mockMvc.perform(get("/api/pips/years"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(26));
    }

    @Test
    void nextCodeReturnsSuggestion() throws Exception {
        given(pipService.suggestNextCode()).willReturn("26_PIP_2");

        mockMvc.perform(get("/api/pips/next-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("26_PIP_2"));
    }

    @Test
    void createReturns201() throws Exception {
        given(pipService.create("26_PIP_1")).willReturn(pip(1L, "26_PIP_1"));

        mockMvc.perform(post("/api/pips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"26_PIP_1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("26_PIP_1"));
    }

    @Test
    void createDuplicateReturns409() throws Exception {
        given(pipService.create("26_PIP_1")).willThrow(new DuplicatePipCodeException("26_PIP_1"));

        mockMvc.perform(post("/api/pips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"26_PIP_1\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void createInvalidReturns400() throws Exception {
        given(pipService.create("bad")).willThrow(new IllegalArgumentException("Invalid PIP code 'bad'"));

        mockMvc.perform(post("/api/pips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"bad\"}"))
                .andExpect(status().isBadRequest());
    }
}
