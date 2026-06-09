package com.testpire.testpire.controller;

import com.testpire.testpire.Controller.InstituteController;
import com.testpire.testpire.dto.response.InstituteListResponseDto;
import com.testpire.testpire.dto.response.InstituteResponseDto;
import com.testpire.testpire.entity.Institute;
import com.testpire.testpire.service.CognitoService;
import com.testpire.testpire.service.InstituteService;
import com.testpire.testpire.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InstituteControllerTest extends BaseControllerTest {

    @Mock InstituteService instituteService;
    @Mock CognitoService cognitoService;
    @Mock UserService userService;

    @InjectMocks
    InstituteController instituteController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(instituteController).build();
    }

    private static Institute sampleEntity() {
        return Institute.builder()
                .id(1L).name("Test Academy").code("TEST01")
                .email("test@academy.com").phone("+911234567890")
                .address("123 Main St").active(true)
                .createdAt(Instant.now()).updatedAt(Instant.now())
                .build();
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    @Test
    void createInstitute_validRequest_returns201() throws Exception {
        when(instituteService.createInstitute(any(), any())).thenReturn(sampleEntity());

        mockMvc.perform(post("/api/institutes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Map.of(
                                "name", "Test Academy",
                                "code", "TEST01",
                                "email", "test@academy.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("TEST01"));
    }

    @Test
    void createInstitute_missingName_returns400() throws Exception {
        mockMvc.perform(post("/api/institutes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Map.of("code", "TEST01"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInstitute_missingCode_returns400() throws Exception {
        mockMvc.perform(post("/api/institutes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Map.of("name", "Test Academy"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInstitute_invalidCodeFormat_returns400() throws Exception {
        // Code must match ^[A-Z0-9]{2,10}$ — lowercase fails
        mockMvc.perform(post("/api/institutes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Map.of(
                                "name", "Test Academy",
                                "code", "invalid-code"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInstitute_duplicateCode_returns400() throws Exception {
        when(instituteService.createInstitute(any(), any()))
                .thenThrow(new IllegalArgumentException("Institute with code TEST01 already exists"));

        mockMvc.perform(post("/api/institutes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Map.of(
                                "name", "Test Academy",
                                "code", "TEST01",
                                "email", "test@academy.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    @Test
    void getInstituteById_found_returns200() throws Exception {
        when(instituteService.getInstituteById(1L)).thenReturn(sampleEntity());

        mockMvc.perform(get("/api/institutes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("TEST01"));
    }

    @Test
    void getInstituteById_notFound_returns400() throws Exception {
        when(instituteService.getInstituteById(999L))
                .thenThrow(new IllegalArgumentException("Institute not found with ID: 999"));

        mockMvc.perform(get("/api/institutes/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getInstituteByCode_returns200() throws Exception {
        when(instituteService.getInstituteByCode("TEST01")).thenReturn(sampleEntity());

        mockMvc.perform(get("/api/institutes/code/TEST01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("TEST01"));
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @Test
    void deleteInstitute_returns200() throws Exception {
        mockMvc.perform(delete("/api/institutes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── SEARCH ───────────────────────────────────────────────────────────────

    @Test
    void searchInstitutes_advancedPost_returns200() throws Exception {
        when(instituteService.searchInstitutesWithSpecification(any()))
                .thenReturn(InstituteListResponseDto.success(
                        List.of(InstituteResponseDto.fromEntity(sampleEntity())), 1, 0, 1));

        mockMvc.perform(post("/api/institutes/search/advanced")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"criteria\":{},\"pagination\":{\"page\":0,\"size\":10}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1));
    }

    @Test
    void searchInstitutes_advancedGet_returns200() throws Exception {
        when(instituteService.searchInstitutesWithSpecification(any()))
                .thenReturn(InstituteListResponseDto.success(
                        List.of(InstituteResponseDto.fromEntity(sampleEntity())), 1, 0, 1));

        mockMvc.perform(get("/api/institutes/search/advanced")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
