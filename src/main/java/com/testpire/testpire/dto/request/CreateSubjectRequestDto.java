package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSubjectRequestDto(
        @NotBlank(message = "Subject name is required")
        @Size(max = 100, message = "Subject name must not exceed 100 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotBlank(message = "Subject code is required")
        @Size(max = 20, message = "Subject code must not exceed 20 characters")
        String code,

        @NotNull(message = "Course ID is required")
        Long courseId,

        @NotNull(message = "Institute ID is required")
        Long instituteId,

        String duration,
        Integer credits,
        String prerequisites
) {}


