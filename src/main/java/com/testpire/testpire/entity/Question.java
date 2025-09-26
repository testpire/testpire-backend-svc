package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = ApplicationConstants.Database.QUESTION_TABLE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE " + ApplicationConstants.Database.QUESTION_TABLE + " SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "question_image_path")
    private String questionImagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficultyLevel;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Option> options;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "correct_option_id")
    private Long correctOptionId;

    @Column(nullable = false)
    private Long instituteId;

    @Column(name = "question_type")
    private String questionType; // MCQ, TRUE_FALSE, FILL_BLANK, etc.

    @Column(name = "marks")
    private Integer marks;

    @Column(name = "negative_marks")
    private Integer negativeMarks;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

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

    @Builder.Default
    private boolean deleted = false;

    @Builder.Default
    private boolean active = true;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}