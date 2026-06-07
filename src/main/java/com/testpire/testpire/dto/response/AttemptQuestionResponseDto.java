package com.testpire.testpire.dto.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * A question as presented to a student during an attempt. Deliberately omits which option is correct.
 * {@code selectedOptionIds} echoes the student's saved answer (for resume), and grading fields are
 * populated only once the attempt is graded AND the test has {@code showAnswers} enabled.
 */
public record AttemptQuestionResponseDto(
        Long questionId,
        String text,
        String questionImagePath,
        String questionType,
        BigDecimal marks,
        Integer sortOrder,
        List<OptionView> options,
        List<Long> selectedOptionIds,
        // Populated only after grading + showAnswers; null otherwise.
        Boolean correct,
        BigDecimal marksAwarded,
        // Correct option ids — revealed only after grading + showAnswers; null otherwise.
        List<Long> correctOptionIds
) {
    public record OptionView(Long id, String text, String optionImagePath, Integer optionOrder) {}
}
