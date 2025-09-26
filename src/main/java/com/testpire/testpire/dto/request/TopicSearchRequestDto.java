package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record TopicSearchRequestDto(
        @NotNull(message = "Institute ID is required")
        Long instituteId,
        
        Long courseId,
        Long subjectId,
        Long chapterId,
        String searchQuery,
        Boolean active
) {}

