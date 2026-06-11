package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = ApplicationConstants.Database.TOPICS_TABLE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @Column(nullable = false)
    private Long instituteId;

    private Integer orderIndex;
    private String duration;
    private String content;
    private String learningOutcomes;

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

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}


