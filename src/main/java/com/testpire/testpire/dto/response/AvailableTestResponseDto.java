package com.testpire.testpire.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A test surfaced to a student as available to take, with their attempt status. Carries no question
 * content — the student fetches questions by starting an attempt.
 */
public record AvailableTestResponseDto(
        Long testId,
        String title,
        String description,
        BigDecimal totalMarks,
        Integer durationMinutes,
        Integer maxAttempts,
        int attemptsUsed,
        Instant availableFrom,
        Instant availableUntil,
        // Id of an existing in-progress attempt the student can resume, if any.
        Long inProgressAttemptId
) {}
