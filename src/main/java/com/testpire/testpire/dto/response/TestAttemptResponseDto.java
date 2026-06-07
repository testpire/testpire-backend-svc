package com.testpire.testpire.dto.response;

import com.testpire.testpire.enums.AttemptStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A student's attempt. While IN_PROGRESS this carries the question list (for taking the test) with
 * score fields null; once graded it carries the score and — if the test enables {@code showAnswers} —
 * the per-question correctness. {@code questions} may be null in summary contexts.
 */
public record TestAttemptResponseDto(
        Long attemptId,
        Long testId,
        String testTitle,
        AttemptStatus status,
        Integer attemptNumber,
        LocalDateTime startedAt,
        LocalDateTime submittedAt,
        LocalDateTime expiresAt,
        BigDecimal score,
        BigDecimal maxScore,
        Boolean passed,
        List<AttemptQuestionResponseDto> questions
) {}
