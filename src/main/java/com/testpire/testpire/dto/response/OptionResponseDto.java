package com.testpire.testpire.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OptionResponseDto(
    Long id,
    String text,
    String optionImagePath,
    Long questionId,
    Integer optionOrder,
    Boolean isCorrect,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy,
    Boolean active
) {}


