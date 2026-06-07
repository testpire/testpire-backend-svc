package com.testpire.testpire.controller;

import com.testpire.testpire.Controller.CourseController;
import com.testpire.testpire.dto.response.CourseListResponseDto;
import com.testpire.testpire.dto.response.CourseResponseDto;
import com.testpire.testpire.service.CourseService;
import com.testpire.testpire.util.JwksJwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest extends BaseControllerTest {

    @Mock
    CourseService courseService;

    @Mock
    JwksJwtUtil jwtUtil;

    @InjectMocks
    CourseController courseController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(courseController).build();
    }

    private static CourseResponseDto sampleCourse() {
        return new CourseResponseDto(1L, "Physics 101", "Intro to Physics", "PHY101",
                2L, "60 hours", "BEGINNER", null, new BigDecimal("25000.00"),
                LocalDateTime.now(), LocalDateTime.now(), "admin@test.com", null, true, List.of());
    }

    // ── CREATE ───────────────────────────────────────────────────────────────

    @Test
    void createCourse_validRequest_returns200() throws Exception {
        when(courseService.createCourse(any())).thenReturn(sampleCourse());

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Map.of(
                                "name", "Physics 101",
                                "code", "PHY101",
                                "instituteId", 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("PHY101"));
    }

    @Test
    void createCourse_missingName_returns400() throws Exception {
        // @NotBlank on name — validation kicks in before service is called
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Map.of(
                                "code", "PHY101",
                                "instituteId", 2))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourse_missingCode_returns400() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Map.of(
                                "name", "Physics 101",
                                "instituteId", 2))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCourse_duplicateCode_returns400() throws Exception {
        when(courseService.createCourse(any()))
                .thenThrow(new IllegalArgumentException("Course with code PHY101 already exists in this institute"));

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Map.of(
                                "name", "Physics 101",
                                "code", "PHY101",
                                "instituteId", 2))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to create course: Course with code PHY101 already exists in this institute"));
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    @Test
    void getCourseById_found_returns200() throws Exception {
        when(courseService.getCourseById(1L)).thenReturn(sampleCourse());

        mockMvc.perform(get("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("PHY101"));
    }

    @Test
    void getCourseById_notFound_returns400() throws Exception {
        when(courseService.getCourseById(999L))
                .thenThrow(new IllegalArgumentException("Course not found with ID: 999"));

        mockMvc.perform(get("/api/courses/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getCourseByCode_withInstituteId_returns200() throws Exception {
        when(courseService.getCourseByCode(eq("PHY101"), eq(2L))).thenReturn(sampleCourse());

        mockMvc.perform(get("/api/courses/code/PHY101").param("instituteId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("PHY101"));
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    @Test
    void updateCourse_validRequest_returns200() throws Exception {
        CourseResponseDto updated = new CourseResponseDto(1L, "Physics Updated", "Updated",
                "PHY101", 2L, "60 hours", "INTERMEDIATE", null, new BigDecimal("30000.00"),
                LocalDateTime.now(), LocalDateTime.now(), "admin@test.com", "admin@test.com", true, List.of());
        when(courseService.updateCourse(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Updated"));
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    @Test
    void deleteCourse_returns200() throws Exception {
        mockMvc.perform(delete("/api/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── SEARCH ───────────────────────────────────────────────────────────────

    @Test
    void searchCourses_advancedPost_returns200() throws Exception {
        when(courseService.searchCoursesWithSpecification(any()))
                .thenReturn(CourseListResponseDto.of(List.of(sampleCourse()), 1L));

        mockMvc.perform(post("/api/courses/search/advanced")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"criteria\":{\"instituteId\":2},\"pagination\":{\"page\":0,\"size\":10}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1));
    }

    @Test
    void searchCourses_advancedGet_returns200() throws Exception {
        when(courseService.searchCoursesWithSpecification(any()))
                .thenReturn(CourseListResponseDto.of(List.of(sampleCourse()), 1L));

        mockMvc.perform(get("/api/courses/search/advanced")
                        .param("instituteId", "2").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1));
    }
}
