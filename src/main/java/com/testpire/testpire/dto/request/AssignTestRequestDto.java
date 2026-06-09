package com.testpire.testpire.dto.request;

import com.testpire.testpire.enums.AssignmentTargetType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Assigns a published test to a target. {@code targetId} is a course id, batch id, or student user
 * id depending on {@code targetType}. The optional window narrows (never widens) the test's own
 * availability window.
 */
public record AssignTestRequestDto(
        @NotNull(message = "targetType is required (COURSE, BATCH or STUDENT)")
        AssignmentTargetType targetType,

        @NotNull(message = "targetId is required")
        Long targetId,

        Instant availableFrom,

        Instant availableUntil
) {}
