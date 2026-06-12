package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.TopicMaterial;
import com.testpire.testpire.enums.MaterialType;
import com.testpire.testpire.enums.TextFormat;

import java.time.Instant;

/**
 * Material metadata. Deliberately omits any presigned download URL — those expire, so callers fetch a
 * fresh one on demand from {@code GET /api/topics/{topicId}/materials/{id}/download-url}. {@code content}
 * is populated for NOTE materials; {@code externalUrl} for LINK; the file fields for PDF/PPT/VIDEO.
 */
public record TopicMaterialResponseDto(
        Long id,
        Long topicId,
        Long instituteId,
        MaterialType type,
        String title,
        String description,
        String fileName,
        String contentType,
        Long sizeBytes,
        String content,
        TextFormat contentFormat,
        String externalUrl,
        Integer sortOrder,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
    public static TopicMaterialResponseDto fromEntity(TopicMaterial m) {
        return new TopicMaterialResponseDto(
                m.getId(),
                m.getTopicId(),
                m.getInstituteId(),
                m.getType(),
                m.getTitle(),
                m.getDescription(),
                m.getFileName(),
                m.getContentType(),
                m.getSizeBytes(),
                m.getContent(),
                m.getContentFormat(),
                m.getExternalUrl(),
                m.getSortOrder(),
                m.getCreatedAt(),
                m.getUpdatedAt(),
                m.getCreatedBy(),
                m.getUpdatedBy()
        );
    }
}
