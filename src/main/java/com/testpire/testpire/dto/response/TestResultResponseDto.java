package com.testpire.testpire.dto.response;

import com.testpire.testpire.enums.AttemptStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Staff view of every student's marks for a test. One {@link StudentResult} per student who has
 * attempted it (students who never started are absent).
 */
public record TestResultResponseDto(
        Long testId,
        String testTitle,
        BigDecimal totalMarks,
        BigDecimal passingMarks,
        int studentCount,
        List<StudentResult> results
) {
    public record StudentResult(
            Long studentUserId,
            String studentUsername,
            String studentName,
            Long attemptId,
            Integer attemptNumber,
            AttemptStatus status,
            BigDecimal score,
            BigDecimal maxScore,
            Boolean passed,
            LocalDateTime submittedAt
    ) {}
}
