package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Links a student (a {@code users} row with role STUDENT) to a {@link Course} + {@link Batch}. A
 * student may have many enrollments, but at most one per course (unique student_user_id + course_id),
 * and the batch must belong to that course. Multi-tenancy is enforced by filtering on
 * {@link #instituteId} in the service layer.
 *
 * <p>This is the source of truth for the course/batch-assignment feature. The legacy free-text
 * {@code StudentDetails.course} string is kept separately and is not synced from here.</p>
 */
@Entity
@Table(name = ApplicationConstants.Database.STUDENT_ENROLLMENTS_TABLE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_user_id", nullable = false)
    private Long studentUserId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "institute_id", nullable = false)
    private Long instituteId;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = ApplicationConstants.Database.CREATED_AT_COLUMN)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = ApplicationConstants.Database.UPDATED_AT_COLUMN)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = ApplicationConstants.Database.CREATED_BY_COLUMN)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
