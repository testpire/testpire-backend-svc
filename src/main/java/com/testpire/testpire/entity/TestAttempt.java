package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.enums.AttemptStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * One student's attempt at a {@link Test}. Holds the timer deadline ({@link #expiresAt}) and, once
 * graded, the final {@link #score}. At most one attempt per (test, student, attempt_number).
 * Multi-tenancy via {@link #instituteId}.
 */
@Entity
@Table(name = ApplicationConstants.Database.TEST_ATTEMPTS_TABLE, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"test_id", "student_user_id", "attempt_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_id", nullable = false)
    private Long testId;

    /** The assignment the student qualified through; informational, may be null. */
    @Column(name = "assignment_id")
    private Long assignmentId;

    @Column(name = "student_user_id", nullable = false)
    private Long studentUserId;

    @Column(name = "institute_id", nullable = false)
    private Long instituteId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Builder.Default
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber = 1;

    @Builder.Default
    @Column(name = "started_at")
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    /** Hard deadline: min(startedAt + duration, effective availableUntil). Null = untimed/no expiry. */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(precision = 8, scale = 2)
    private BigDecimal score;

    @Column(name = "max_score", precision = 8, scale = 2)
    private BigDecimal maxScore;

    private Boolean passed;

    @Builder.Default
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestAttemptAnswer> answers = new ArrayList<>();
}
