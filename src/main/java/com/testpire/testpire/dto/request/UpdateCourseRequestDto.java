package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UpdateCourseRequestDto(
        @Size(max = 100, message = "Course name must not exceed 100 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Size(max = 20, message = "Course code must not exceed 20 characters")
        String code,

        String duration,
        String level,
        String prerequisites,
        Boolean active,

        // null = leave unchanged. Setting a value updates the course fee; non-overridden batches follow it.
        @DecimalMin(value = "0", message = "Fee must not be negative")
        BigDecimal fee,

        // When non-null, replaces the course's full subject set (identified by code).
        List<String> subjectCodes
) {}


