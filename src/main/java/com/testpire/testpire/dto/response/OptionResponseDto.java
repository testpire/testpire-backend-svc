package com.testpire.testpire.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record OptionResponseDto(
    Long id,
    String text,
    String optionImagePath,
    Long questionId,
    Integer optionOrder,
    Boolean isCorrect,
    Instant createdAt,
    Instant updatedAt,
    String createdBy,
    String updatedBy
) {}


