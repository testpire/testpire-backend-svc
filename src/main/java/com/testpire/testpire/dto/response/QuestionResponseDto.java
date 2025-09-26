package com.testpire.testpire.dto.response;

import com.testpire.testpire.enums.DifficultyLevel;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record QuestionResponseDto(
    Long id,
    String text,
    String questionImagePath,
    DifficultyLevel difficultyLevel,
    Long topicId,
    String topicName,
    Long correctOptionId,
    Long instituteId,
    String instituteName,
    String questionType,
    Integer marks,
    Integer negativeMarks,
    String explanation,
    List<OptionResponseDto> options,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy,
    Boolean active
) {}

