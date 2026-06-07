package com.testpire.testpire.service;

import com.testpire.testpire.service.TestAttemptService.GradedAnswer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the objective auto-grading math ({@link TestAttemptService#gradeAnswer}). Covers the
 * all-or-nothing rule for single- and multi-select, negative marking, and the unanswered case.
 */
class TestAttemptServiceTest {

    private static final BigDecimal MARKS = new BigDecimal("4.00");
    private static final BigDecimal NEGATIVE = new BigDecimal("1.00");

    @Test
    void correctSingleSelect_awardsFullMarks() {
        GradedAnswer g = TestAttemptService.gradeAnswer(Set.of(10L), Set.of(10L), MARKS, NEGATIVE, true);
        assertThat(g.isCorrect()).isTrue();
        assertThat(g.awarded()).isEqualByComparingTo(MARKS);
    }

    @Test
    void wrongAnswer_withNegativeMarking_deductsNegativeMarks() {
        GradedAnswer g = TestAttemptService.gradeAnswer(Set.of(11L), Set.of(10L), MARKS, NEGATIVE, true);
        assertThat(g.isCorrect()).isFalse();
        assertThat(g.awarded()).isEqualByComparingTo(NEGATIVE.negate());
    }

    @Test
    void wrongAnswer_withoutNegativeMarking_awardsZero() {
        GradedAnswer g = TestAttemptService.gradeAnswer(Set.of(11L), Set.of(10L), MARKS, NEGATIVE, false);
        assertThat(g.isCorrect()).isFalse();
        assertThat(g.awarded()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void multiSelect_exactMatch_isCorrect_regardlessOfOrder() {
        GradedAnswer g = TestAttemptService.gradeAnswer(Set.of(12L, 10L), Set.of(10L, 12L), MARKS, NEGATIVE, true);
        assertThat(g.isCorrect()).isTrue();
        assertThat(g.awarded()).isEqualByComparingTo(MARKS);
    }

    @Test
    void multiSelect_partialMatch_isWrong_allOrNothing() {
        // Selected a subset of the correct options -> still wrong (no partial credit).
        GradedAnswer g = TestAttemptService.gradeAnswer(Set.of(10L), Set.of(10L, 12L), MARKS, NEGATIVE, false);
        assertThat(g.isCorrect()).isFalse();
        assertThat(g.awarded()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void multiSelect_extraOption_isWrong() {
        GradedAnswer g = TestAttemptService.gradeAnswer(Set.of(10L, 12L, 13L), Set.of(10L, 12L), MARKS, NEGATIVE, true);
        assertThat(g.isCorrect()).isFalse();
        assertThat(g.awarded()).isEqualByComparingTo(NEGATIVE.negate());
    }

    @Test
    void unanswered_isNullCorrectness_andZeroMarks_evenWithNegativeMarking() {
        GradedAnswer g = TestAttemptService.gradeAnswer(Set.of(), Set.of(10L), MARKS, NEGATIVE, true);
        assertThat(g.isCorrect()).isNull();
        assertThat(g.awarded()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
