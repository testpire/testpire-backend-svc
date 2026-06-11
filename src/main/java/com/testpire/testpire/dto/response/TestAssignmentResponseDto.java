package com.testpire.testpire.dto.response;

import com.testpire.testpire.entity.TestAssignment;
import com.testpire.testpire.enums.AssignmentTargetType;

import java.time.Instant;

public record TestAssignmentResponseDto(
        Long id,
        Long testId,
        AssignmentTargetType targetType,
        Long targetId,
        // Human-friendly name of the target (course/batch name or student username); may be null.
        String targetName,
        Instant availableFrom,
        Instant availableUntil,
        Instant assignedAt,
        String assignedBy
) {
    public static TestAssignmentResponseDto fromEntity(TestAssignment a, String targetName) {
        return new TestAssignmentResponseDto(
                a.getId(),
                a.getTestId(),
                a.getTargetType(),
                a.getTargetId(),
                targetName,
                a.getAvailableFrom(),
                a.getAvailableUntil(),
                a.getAssignedAt(),
                a.getAssignedBy()
        );
    }
}
