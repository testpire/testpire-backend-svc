package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.enums.AssignmentTargetType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Logical assignment of a {@link Test} to a course, batch, or individual student. This is NOT
 * materialized per student: {@code TestResolutionService} computes which students a COURSE/BATCH
 * assignment reaches by joining {@code student_enrollments} at query time, so students who enroll
 * after the assignment inherit it automatically.
 *
 * <p>{@link #targetId} is resolved against a different table per {@link #targetType}
 * (COURSE -> courses, BATCH -> batches, STUDENT -> users). Multi-tenancy via {@link #instituteId}.</p>
 */
@Entity
@Table(name = ApplicationConstants.Database.TEST_ASSIGNMENTS_TABLE, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"test_id", "target_type", "target_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "test_id", nullable = false)
    private Long testId;

    @Column(name = "institute_id", nullable = false)
    private Long instituteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private AssignmentTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /** Optional per-assignment window start; effective start is the later of this and the test's. */
    @Column(name = "available_from")
    private Instant availableFrom;

    /** Optional per-assignment expiry; effective expiry is the earlier of this and the test's. */
    @Column(name = "available_until")
    private Instant availableUntil;

    @Builder.Default
    @Column(name = "assigned_at", updatable = false)
    private Instant assignedAt = Instant.now();

    @Column(name = "assigned_by")
    private String assignedBy;
}
