package com.testpire.testpire.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record BulkUploadResponseDto(
    Integer totalProcessed,
    Integer successfulUploads,
    Integer failedUploads,
    List<String> errors,
    List<QuestionResponseDto> uploadedQuestions
) {}

