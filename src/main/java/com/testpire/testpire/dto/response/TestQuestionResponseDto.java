package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.Question;
import com.testpire.testpire.entity.TestQuestion;

import java.math.BigDecimal;
import java.util.List;

/**
 * A question as it appears inside a test (staff view — includes correct answers). For the student
 * test-taking view see {@link AttemptQuestionResponseDto}, which omits correctness.
 */
public record TestQuestionResponseDto(
        Long testQuestionId,
        Long questionId,
        String text,
        String questionImagePath,
        String questionType,
        // Effective marks for this question in this test (override if set, else the question's own marks).
        BigDecimal marks,
        BigDecimal negativeMarks,
        Integer sortOrder,
        List<OptionView> options
) {
    public record OptionView(Long id, String text, String optionImagePath, Integer optionOrder, boolean correct) {}

    public static TestQuestionResponseDto fromEntity(TestQuestion tq, BigDecimal effectiveMarks,
                                                     BigDecimal effectiveNegative) {
        Question q = tq.getQuestion();
        List<OptionView> opts = q.getOptions() == null ? List.of() : q.getOptions().stream()
                .map(o -> new OptionView(o.getId(), o.getText(), o.getOptionImagePath(),
                        o.getOptionOrder(), o.isCorrect()))
                .toList();
        return new TestQuestionResponseDto(
                tq.getId(),
                q.getId(),
                q.getText(),
                q.getQuestionImagePath(),
                q.getQuestionType(),
                effectiveMarks,
                effectiveNegative,
                tq.getSortOrder(),
                opts
        );
    }
}
