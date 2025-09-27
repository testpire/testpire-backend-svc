package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCourseRequestDto(
        @NotBlank(message = "Course name is required")
        @Size(max = 100, message = "Course name must not exceed 100 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotBlank(message = "Course code is required")
        @Size(max = 20, message = "Course code must not exceed 20 characters")
        String code,

        Long instituteId, // Will be extracted from JWT token

        String duration,
        String level,
        String prerequisites
) {}


