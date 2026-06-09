package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Creates a test (in DRAFT). Questions are added separately via the questions endpoints, and
 * {@code totalMarks} is derived from them — never client-supplied. {@code instituteId} is honored
 * only for SUPER_ADMIN; other callers are scoped to their JWT institute.
 */
public record CreateTestRequestDto(
        @NotBlank(message = "Test title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        @Min(value = 1, message = "Duration must be at least 1 minute")
        Integer durationMinutes,

        @Min(value = 1, message = "Max attempts must be at least 1")
        Integer maxAttempts,

        BigDecimal passingMarks,

        Boolean negativeMarking,

        Boolean shuffleQuestions,

        Boolean showAnswers,

        Instant availableFrom,

        Instant availableUntil,

        Long instituteId
) {}
