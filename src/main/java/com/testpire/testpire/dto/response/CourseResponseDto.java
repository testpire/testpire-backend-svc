package com.testpire.testpire.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.testpire.testpire.entity.Course;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public record CourseResponseDto(
        Long id,
        String name,
        String description,
        String code,
        Long instituteId,
        String duration,
        String level,
        String prerequisites,
        BigDecimal fee,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<SubjectResponseDto> subjects
) {
    public static CourseResponseDto fromEntity(Course course) {
        return fromEntity(course, Set.of());
    }

    public static CourseResponseDto fromEntity(Course course, Set<String> includes) {
        return new CourseResponseDto(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getCode(),
                course.getInstituteId(),
                course.getDuration(),
                course.getLevel(),
                course.getPrerequisites(),
                course.getFee(),
                course.getCreatedAt(),
                course.getUpdatedAt(),
                course.getCreatedBy(),
                course.getUpdatedBy(),
                includes.contains("subjects") && course.getSubjects() != null ?
                    course.getSubjects().stream()
                        .map(subject -> SubjectResponseDto.fromEntity(subject, includes))
                        .toList() :
                    null
        );
    }
}


