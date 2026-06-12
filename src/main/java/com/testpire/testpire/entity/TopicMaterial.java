package com.testpire.testpire.entity;

import com.testpire.testpire.constants.ApplicationConstants;
import com.testpire.testpire.enums.MaterialType;
import com.testpire.testpire.enums.TextFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * A teaching material (PPT / PDF / video / inline note / external link) attached to a {@link Topic}.
 *
 * <p>{@code topicId} and {@code instituteId} are stored as scalars (mirroring {@link Topic#getInstituteId()})
 * — the row is filtered on {@code instituteId} for multi-tenancy and on {@code topicId} for listing.
 * File-backed materials ({@link MaterialType#isFileBacked()}) carry the S3 {@code s3Key} + file metadata;
 * {@code NOTE} carries {@code content}/{@code contentFormat}; {@code LINK} carries {@code externalUrl}.</p>
 */
@Entity
@Table(name = ApplicationConstants.Database.TOPIC_MATERIALS_TABLE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = ApplicationConstants.Database.INSTITUTE_ID_COLUMN, nullable = false)
    private Long instituteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaterialType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // --- file-backed (PDF / PPT / VIDEO) ---
    @Column(name = "s3_key", length = 512)
    private String s3Key;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    // --- inline note (NOTE) ---
    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_format", length = 20)
    private TextFormat contentFormat;

    // --- external link (LINK) ---
    @Column(name = "external_url", length = 1024)
    private String externalUrl;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

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
