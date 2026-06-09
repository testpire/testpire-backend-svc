package com.testpire.testpire.dto.response;

import com.testpire.testpire.enums.AttemptStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One row in a student's attempt history (the "Results" tab list). A trimmed-down sibling of
 * {@link TestAttemptResponseDto} without the per-question breakdown — the UI fetches the full
 * {@link TestAttemptResponseDto} via {@code GET /api/student/tests/attempts/{attemptId}} when a
 * row is opened. Score fields are null until the attempt is graded.
 */
public record AttemptSummaryResponseDto(
        Long attemptId,
        Long testId,
        String testTitle,
        AttemptStatus status,
        Integer attemptNumber,
        BigDecimal score,
        BigDecimal maxScore,
        Boolean passed,
        Instant submittedAt
) {}
