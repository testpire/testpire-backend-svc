package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A student's answer to one {@link Question} within a {@link TestAttempt}. {@link #selectedOptionIds}
 * is a CSV of chosen option ids (one for single-select, many for multi-select). Grading fields
 * ({@link #isCorrect}, {@link #marksAwarded}) are populated when the attempt is graded.
 * At most one answer per (attempt, question).
 */
@Entity
@Table(name = ApplicationConstants.Database.TEST_ATTEMPT_ANSWERS_TABLE, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"attempt_id", "question_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private TestAttempt attempt;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    /** CSV of selected option ids (e.g. "12" or "12,15"). Null/blank = unanswered. */
    @Column(name = "selected_option_ids", columnDefinition = "TEXT")
    private String selectedOptionIds;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "marks_awarded", precision = 8, scale = 2)
    private BigDecimal marksAwarded;

    @Builder.Default
    @Column(name = "answered_at")
    private Instant answeredAt = Instant.now();
}
