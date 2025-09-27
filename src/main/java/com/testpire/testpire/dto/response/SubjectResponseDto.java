package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.Subject;

import java.time.LocalDateTime;
import java.util.List;

public record SubjectResponseDto(
        Long id,
        String name,
        String description,
        String code,
        Long courseId,
        Long instituteId,
        String duration,
        Integer credits,
        String prerequisites,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        boolean active,
        List<ChapterResponseDto> chapters
) {
    public static SubjectResponseDto fromEntity(Subject subject) {
        return new SubjectResponseDto(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                subject.getCode(),
                subject.getCourse().getId(),
                subject.getInstituteId(),
                subject.getDuration(),
                subject.getCredits(),
                subject.getPrerequisites(),
                subject.getCreatedAt(),
                subject.getUpdatedAt(),
                subject.getCreatedBy(),
                subject.getUpdatedBy(),
                subject.isActive(),
                subject.getChapters() != null ? 
                    subject.getChapters().stream()
                        .map(ChapterResponseDto::fromEntity)
                        .toList() : 
                    List.of()
        );
    }
}


