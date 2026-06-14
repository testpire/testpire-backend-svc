package com.testpire.testpire.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.testpire.testpire.entity.Chapter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record ChapterResponseDto(
        Long id,
        String name,
        String description,
        String code,
        Long subjectId,
        Long instituteId,
        Integer orderIndex,
        String duration,
        String objectives,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<TopicResponseDto> topics
) {
    public static ChapterResponseDto fromEntity(Chapter chapter) {
        return fromEntity(chapter, Set.of());
    }

    public static ChapterResponseDto fromEntity(Chapter chapter, Set<String> includes) {
        return new ChapterResponseDto(
                chapter.getId(),
                chapter.getName(),
                chapter.getDescription(),
                chapter.getCode(),
                chapter.getSubject().getId(),
                chapter.getInstituteId(),
                chapter.getOrderIndex(),
                chapter.getDuration(),
                chapter.getObjectives(),
                chapter.getCreatedAt(),
                chapter.getUpdatedAt(),
                chapter.getCreatedBy(),
                chapter.getUpdatedBy(),
                includes.contains("topics") && chapter.getTopics() != null ?
                    chapter.getTopics().stream()
                        .map(TopicResponseDto::fromEntity)
                        .toList() :
                    null
        );
    }
}


