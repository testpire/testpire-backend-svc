package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Partial update of a batch. All fields optional; null = leave unchanged. The parent course cannot
 * be changed here (a batch stays under the course it was created in).
 */
public record UpdateBatchRequestDto(
        @Size(max = 100, message = "Batch name must not exceed 100 characters")
        String name,

        @Size(max = 20, message = "Batch code must not exceed 20 characters")
        String code,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        LocalDate startDate,

        LocalDate endDate,

        @Min(value = 1, message = "Capacity must be at least 1")
        Integer capacity,

        Boolean active
) {}
