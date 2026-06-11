package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = ApplicationConstants.Database.COURSES_TABLE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private Long instituteId;

    private String duration;
    private String level;
    private String prerequisites;

    /** Course fee; {@code null} = not set. Batches inherit this unless they set their own override. */
    @Column(precision = 12, scale = 2)
    private BigDecimal fee;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "course_subjects",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    @Builder.Default
    private List<Subject> subjects = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}


