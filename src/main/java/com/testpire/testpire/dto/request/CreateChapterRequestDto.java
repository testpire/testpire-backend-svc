package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateChapterRequestDto(
        @NotBlank(message = "Chapter name is required")
        @Size(max = 100, message = "Chapter name must not exceed 100 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotBlank(message = "Chapter code is required")
        @Size(max = 20, message = "Chapter code must not exceed 20 characters")
        String code,

        @NotNull(message = "Subject ID is required")
        Long subjectId,

        @NotNull(message = "Institute ID is required")
        Long instituteId,

        Integer orderIndex,
        String duration,
        String objectives
) {}

