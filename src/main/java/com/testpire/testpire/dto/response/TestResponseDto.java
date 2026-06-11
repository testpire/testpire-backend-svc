package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.Test;
import com.testpire.testpire.enums.TestStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Full staff view of a test, including its questions (with correct answers). {@code questions} may be
 * null in list views where they are not loaded.
 */
public record TestResponseDto(
        Long id,
        String title,
        String description,
        Long instituteId,
        BigDecimal totalMarks,
        BigDecimal passingMarks,
        Integer durationMinutes,
        Integer maxAttempts,
        boolean negativeMarking,
        boolean shuffleQuestions,
        boolean showAnswers,
        TestStatus status,
        Instant availableFrom,
        Instant availableUntil,
        int questionCount,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy,
        List<TestQuestionResponseDto> questions
) {
    /** Summary (no question list) — for list views. */
    public static TestResponseDto summary(Test t, int questionCount) {
        return build(t, questionCount, null);
    }

    /** Detailed view with the question list. */
    public static TestResponseDto detail(Test t, List<TestQuestionResponseDto> questions) {
        return build(t, questions == null ? 0 : questions.size(), questions);
    }

    private static TestResponseDto build(Test t, int questionCount, List<TestQuestionResponseDto> questions) {
        return new TestResponseDto(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getInstituteId(),
                t.getTotalMarks(),
                t.getPassingMarks(),
                t.getDurationMinutes(),
                t.getMaxAttempts(),
                t.isNegativeMarking(),
                t.isShuffleQuestions(),
                t.isShowAnswers(),
                t.getStatus(),
                t.getAvailableFrom(),
                t.getAvailableUntil(),
                questionCount,
                t.getCreatedAt(),
                t.getUpdatedAt(),
                t.getCreatedBy(),
                t.getUpdatedBy(),
                questions
        );
    }
}
