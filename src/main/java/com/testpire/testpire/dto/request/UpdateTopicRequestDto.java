package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateTopicRequestDto(
        @Size(max = 100, message = "Topic name must not exceed 100 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Size(max = 20, message = "Topic code must not exceed 20 characters")
        String code,

        Integer orderIndex,
        String duration,
        String content,
        String learningOutcomes,
        Boolean active
) {}
