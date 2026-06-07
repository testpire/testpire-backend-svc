package com.testpire.testpire.dto.request;

import jakarta.validation.Valid;

import java.util.List;

/**
 * Submits an attempt for grading. Answers may be saved incrementally via the save-answer endpoint and
 * this body left empty, or the full set of answers may be sent here in one shot — any answers present
 * are upserted before grading.
 */
public record SubmitAttemptRequestDto(
        @Valid
        List<SubmitAnswerRequestDto> answers
) {}
