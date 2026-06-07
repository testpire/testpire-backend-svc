package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Saves (upserts) a student's answer to a single question within an in-progress attempt. An empty or
 * null {@code selectedOptionIds} clears the answer (treated as unanswered).
 */
public record SubmitAnswerRequestDto(
        @NotNull(message = "questionId is required")
        Long questionId,

        List<Long> selectedOptionIds
) {}
