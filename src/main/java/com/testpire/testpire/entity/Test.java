package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.enums.TestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A curated collection of {@link Question}s taken by students as a timed exam. Questions are joined
 * via {@link TestQuestion} (with optional per-test mark overrides). Assignment to a course/batch/student
 * lives in {@link TestAssignment}; a student's run lives in {@link TestAttempt}.
 *
 * <p>Multi-tenancy is enforced by filtering on {@link #instituteId} in the service layer.</p>
 */
@Entity
@Table(name = ApplicationConstants.Database.TESTS_TABLE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "institute_id", nullable = false)
    private Long instituteId;

    /** Derived sum of effective per-question marks; recomputed whenever questions change. */
    @Builder.Default
    @Column(name = "total_marks", precision = 8, scale = 2)
    private BigDecimal totalMarks = BigDecimal.ZERO;

    /** Pass mark; {@code null} = no pass/fail threshold. */
    @Column(name = "passing_marks", precision = 8, scale = 2)
    private BigDecimal passingMarks;

    /** Time limit in minutes; {@code null} = untimed. */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Builder.Default
    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 1;

    @Builder.Default
    @Column(name = "negative_marking", nullable = false)
    private boolean negativeMarking = false;

    @Builder.Default
    @Column(name = "shuffle_questions", nullable = false)
    private boolean shuffleQuestions = false;

    @Builder.Default
    @Column(name = "show_answers", nullable = false)
    private boolean showAnswers = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TestStatus status = TestStatus.DRAFT;

    /** Test-level availability window start; {@code null} = always open once published. */
    @Column(name = "available_from")
    private Instant availableFrom;

    /** Test-level expiry; {@code null} = no expiry. An assignment may narrow but not widen this. */
    @Column(name = "available_until")
    private Instant availableUntil;

    @Builder.Default
    @Column(name = ApplicationConstants.Database.CREATED_AT_COLUMN)
    private Instant createdAt = Instant.now();

    @Builder.Default
    @Column(name = ApplicationConstants.Database.UPDATED_AT_COLUMN)
    private Instant updatedAt = Instant.now();

    @Column(name = ApplicationConstants.Database.CREATED_BY_COLUMN)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Builder.Default
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<TestQuestion> testQuestions = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
