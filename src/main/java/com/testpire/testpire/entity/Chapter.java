package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = ApplicationConstants.Database.CHAPTERS_TABLE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chapter {

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
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private Long instituteId;

    private Integer orderIndex;
    private String duration;
    private String objectives;

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

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Topic> topics = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}


