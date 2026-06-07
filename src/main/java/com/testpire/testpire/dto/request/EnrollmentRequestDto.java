package com.testpire.testpire.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * One course+batch assignment for a student. Both ids are required; the batch must belong to the
 * course and both must be in the student's institute. Used inside {@code CreateStudentRequestDto} /
 * {@code UpdateStudentRequestDto}.
 */
public record EnrollmentRequestDto(
        @NotNull(message = "Course ID is required")
        Long courseId,

        @NotNull(message = "Batch ID is required")
        Long batchId
) {}
