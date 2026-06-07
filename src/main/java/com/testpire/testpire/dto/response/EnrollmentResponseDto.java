package com.testpire.testpire.dto.response;

/**
 * A student's course+batch enrollment, with the course/batch names resolved for display.
 */
public record EnrollmentResponseDto(
        Long enrollmentId,
        Long courseId,
        String courseName,
        Long batchId,
        String batchName
) {}
