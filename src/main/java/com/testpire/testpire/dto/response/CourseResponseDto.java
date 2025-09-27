package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.Course;

import java.time.LocalDateTime;
import java.util.List;

public record CourseResponseDto(
        Long id,
        String name,
        String description,
        String code,
        Long instituteId,
        String duration,
        String level,
        String prerequisites,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        boolean active,
        List<SubjectResponseDto> subjects
) {
    public static CourseResponseDto fromEntity(Course course) {
        return new CourseResponseDto(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getCode(),
                course.getInstituteId(),
                course.getDuration(),
                course.getLevel(),
                course.getPrerequisites(),
                course.getCreatedAt(),
                course.getUpdatedAt(),
                course.getCreatedBy(),
                course.getUpdatedBy(),
                course.isActive(),
                course.getSubjects() != null ? 
                    course.getSubjects().stream()
                        .map(SubjectResponseDto::fromEntity)
                        .toList() : 
                    List.of()
        );
    }
}


