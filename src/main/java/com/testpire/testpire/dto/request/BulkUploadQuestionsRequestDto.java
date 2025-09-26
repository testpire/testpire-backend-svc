package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record BulkUploadQuestionsRequestDto(
    Long topicId,
    
    @NotNull(message = "Institute ID is required")
    Long instituteId,
    
    @NotNull(message = "CSV file is required")
    String csvFileBase64
) {}

