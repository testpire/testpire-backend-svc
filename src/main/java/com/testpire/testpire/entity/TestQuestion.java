package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Join row placing a {@link Question} into a {@link Test}, with optional per-test mark overrides.
 * A question appears at most once per test (unique test_id + question_id).
 */
@Entity
@Table(name = ApplicationConstants.Database.TEST_QUESTIONS_TABLE, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"test_id", "question_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /** Per-test marks override; {@code null} = fall back to {@link Question#getMarks()}. */
    @Column(precision = 8, scale = 2)
    private BigDecimal marks;

    /** Per-test negative-marks override; {@code null} = fall back to {@link Question#getNegativeMarks()}. */
    @Column(name = "negative_marks", precision = 8, scale = 2)
    private BigDecimal negativeMarks;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Builder.Default
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    @Column(name = "added_by")
    private String addedBy;
}
