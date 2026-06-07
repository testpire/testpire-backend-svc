package com.testpire.testpire.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * Adds (or upserts) a set of questions onto a test. Each item carries an optional per-test marks
 * override; if {@code marks} is null the question's own {@code marks} is used. Re-sending a question
 * already on the test updates its marks/order rather than erroring.
 */
public record AddTestQuestionsRequestDto(
        @NotEmpty(message = "At least one question is required")
        @Valid
        List<TestQuestionItem> questions
) {
    public record TestQuestionItem(
            @NotNull(message = "questionId is required")
            Long questionId,

            // Per-test marks override; null = use the question's own marks.
            BigDecimal marks,

            // Per-test negative-marks override; null = use the question's own negative marks.
            BigDecimal negativeMarks,

            // Display order within the test; null = appended after existing questions.
            Integer sortOrder
    ) {}
}
