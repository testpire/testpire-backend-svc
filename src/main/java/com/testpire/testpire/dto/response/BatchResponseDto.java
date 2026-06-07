package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.Batch;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BatchResponseDto(
        Long id,
        String name,
        String code,
        String description,
        Long courseId,
        Long instituteId,
        LocalDate startDate,
        LocalDate endDate,
        Integer capacity,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {
    public static BatchResponseDto fromEntity(Batch batch) {
        return new BatchResponseDto(
                batch.getId(),
                batch.getName(),
                batch.getCode(),
                batch.getDescription(),
                batch.getCourseId(),
                batch.getInstituteId(),
                batch.getStartDate(),
                batch.getEndDate(),
                batch.getCapacity(),
                batch.isActive(),
                batch.getCreatedAt(),
                batch.getUpdatedAt(),
                batch.getCreatedBy(),
                batch.getUpdatedBy()
        );
    }
}
