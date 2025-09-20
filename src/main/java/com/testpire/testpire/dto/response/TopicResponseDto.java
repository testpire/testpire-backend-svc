package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.Topic;

import java.time.LocalDateTime;

public record TopicResponseDto(
        Long id,
        String name,
        String description,
        String code,
        Long chapterId,
        Long instituteId,
        Integer orderIndex,
        String duration,
        String content,
        String learningOutcomes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        boolean active
) {
    public static TopicResponseDto fromEntity(Topic topic) {
        return new TopicResponseDto(
                topic.getId(),
                topic.getName(),
                topic.getDescription(),
                topic.getCode(),
                topic.getChapter().getId(),
                topic.getInstituteId(),
                topic.getOrderIndex(),
                topic.getDuration(),
                topic.getContent(),
                topic.getLearningOutcomes(),
                topic.getCreatedAt(),
                topic.getUpdatedAt(),
                topic.getCreatedBy(),
                topic.getUpdatedBy(),
                topic.isActive()
        );
    }
}
