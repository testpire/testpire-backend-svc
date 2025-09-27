package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTopicRequestDto(
        @NotBlank(message = "Topic name is required")
        @Size(max = 100, message = "Topic name must not exceed 100 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotBlank(message = "Topic code is required")
        @Size(max = 20, message = "Topic code must not exceed 20 characters")
        String code,

        @NotNull(message = "Chapter ID is required")
        Long chapterId,

        Long instituteId, // Will be extracted from JWT token

        Integer orderIndex,
        String duration,
        String content,
        String learningOutcomes
) {}


