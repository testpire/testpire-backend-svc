package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Partial update of a test's metadata. All fields are optional; null = leave unchanged. Questions,
 * status, and derived {@code totalMarks} are managed through dedicated endpoints, not here.
 */
public record UpdateTestRequestDto(
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

        Instant availableUntil
) {}
