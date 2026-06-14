package com.testpire.testpire.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.testpire.testpire.entity.Subject;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record SubjectResponseDto(
        Long id,
        String name,
        String description,
        String code,
        List<Long> courseIds,
        Long instituteId,
        String duration,
        Integer credits,
        String prerequisites,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<ChapterResponseDto> chapters
) {
    public static SubjectResponseDto fromEntity(Subject subject) {
        return fromEntity(subject, Set.of());
    }

    public static SubjectResponseDto fromEntity(Subject subject, Set<String> includes) {
        return new SubjectResponseDto(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                subject.getCode(),
                subject.getCourses() != null
                        ? subject.getCourses().stream().map(c -> c.getId()).toList()
                        : List.of(),
                subject.getInstituteId(),
                subject.getDuration(),
                subject.getCredits(),
                subject.getPrerequisites(),
                subject.getCreatedAt(),
                subject.getUpdatedAt(),
                subject.getCreatedBy(),
                subject.getUpdatedBy(),
                includes.contains("chapters") && subject.getChapters() != null ?
                    subject.getChapters().stream()
                        .map(chapter -> ChapterResponseDto.fromEntity(chapter, includes))
                        .toList() :
                    null
        );
    }
}


