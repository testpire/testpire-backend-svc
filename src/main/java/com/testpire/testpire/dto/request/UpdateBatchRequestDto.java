package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Partial update of a batch. All fields optional; null = leave unchanged. The parent course cannot
 * be changed here (a batch stays under the course it was created in).
 *
 * <p>Fee semantics: send {@code fee} to set/replace this batch's override. Send {@code inheritFee=true}
 * to clear the override so the batch goes back to inheriting the course fee. Sending both is an error.</p>
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

        // Set/replace this batch's fee override. null = leave the fee unchanged (use inheritFee to clear).
        @DecimalMin(value = "0", message = "Fee must not be negative")
        BigDecimal fee,

        // true = clear any override so the batch inherits the course fee again. Cannot be combined with a fee.
        Boolean inheritFee,

        Boolean active
) {}
