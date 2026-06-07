package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A batch/cohort under a {@link Course} (e.g. course "IIT" -> batches "IIT-B01", "IIT-B02"). A course
 * has many batches; a batch belongs to exactly one course. Multi-tenancy is enforced by filtering on
 * {@link #instituteId} in the service layer. Soft-deleted (mirrors {@link Course}).
 */
@Entity
@Table(name = ApplicationConstants.Database.BATCHES_TABLE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE " + ApplicationConstants.Database.BATCHES_TABLE + " SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "institute_id", nullable = false)
    private Long instituteId;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private Integer capacity;

    /**
     * Per-batch fee OVERRIDE. {@code null} = inherit the parent {@link Course}'s fee; a value overrides
     * it for this batch only. The effective fee is resolved as {@code COALESCE(fee, course.fee)}.
     */
    @Column(precision = 12, scale = 2)
    private BigDecimal fee;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean deleted = false;

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
