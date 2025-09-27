package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.DifficultyLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateQuestionRequestDto(
    @NotBlank(message = "Question text is required")
    @Size(max = 2000, message = "Question text cannot exceed 2000 characters")
    String text,
    
    String questionImagePath,
    
    @NotNull(message = "Difficulty level is required")
    DifficultyLevel difficultyLevel,
    
    Long topicId,
    
    Long instituteId, // Will be extracted from JWT token
    
    @NotBlank(message = "Question type is required")
    @Size(max = 50, message = "Question type cannot exceed 50 characters")
    String questionType,
    
    @Min(value = 1, message = "Marks must be at least 1")
    @Max(value = 100, message = "Marks cannot exceed 100")
    Integer marks,
    
    @Min(value = 0, message = "Negative marks cannot be negative")
    @Max(value = 50, message = "Negative marks cannot exceed 50")
    Integer negativeMarks,
    
    @Size(max = 2000, message = "Explanation cannot exceed 2000 characters")
    String explanation,
    
    @NotEmpty(message = "At least one option is required")
    @Size(min = 2, max = 6, message = "Question must have between 2 and 6 options")
    @Valid
    List<CreateOptionRequestDto> options
) {}


