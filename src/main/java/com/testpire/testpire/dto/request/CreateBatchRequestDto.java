package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Creates a batch under a course. {@code instituteId} is honored only for SUPER_ADMIN; other callers
 * are scoped to their JWT institute. The batch name is unique within its course.
 */
public record CreateBatchRequestDto(
        @NotBlank(message = "Batch name is required")
        @Size(max = 100, message = "Batch name must not exceed 100 characters")
        String name,

        @Size(max = 20, message = "Batch code must not exceed 20 characters")
        String code,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotNull(message = "Course ID is required")
        Long courseId,

        LocalDate startDate,

        LocalDate endDate,

        @Min(value = 1, message = "Capacity must be at least 1")
        Integer capacity,

        Long instituteId
) {}
