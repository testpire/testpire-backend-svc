package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.Chapter;

import java.time.LocalDateTime;
import java.util.List;

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
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        boolean active,
        List<TopicResponseDto> topics
) {
    public static ChapterResponseDto fromEntity(Chapter chapter) {
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
                chapter.isActive(),
                chapter.getTopics() != null ? 
                    chapter.getTopics().stream()
                        .map(TopicResponseDto::fromEntity)
                        .toList() : 
                    List.of()
        );
    }
}


