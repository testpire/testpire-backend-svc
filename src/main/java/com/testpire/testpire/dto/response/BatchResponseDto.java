package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.Batch;

import java.math.BigDecimal;
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
        // Raw per-batch override (null = inheriting the course fee).
        BigDecimal fee,
        // Fee actually charged: the override if set, otherwise the course fee (may be null if neither is set).
        BigDecimal effectiveFee,
        // true when this batch has no override and is following the course fee.
        boolean feeInherited,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) {
    /**
     * @param courseFee the parent course's fee, used to resolve {@code effectiveFee} when this batch
     *                  has no override. Pass the course's current fee (may be null).
     */
    public static BatchResponseDto fromEntity(Batch batch, BigDecimal courseFee) {
        BigDecimal override = batch.getFee();
        boolean inherited = override == null;
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
                override,
                inherited ? courseFee : override,
                inherited,
                batch.isActive(),
                batch.getCreatedAt(),
                batch.getUpdatedAt(),
                batch.getCreatedBy(),
                batch.getUpdatedBy()
        );
    }
}
