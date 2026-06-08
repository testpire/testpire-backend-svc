package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequirePermission;
import com.testpire.testpire.dto.request.SubmitAnswerRequestDto;
import com.testpire.testpire.dto.request.SubmitAttemptRequestDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.dto.response.AttemptSummaryResponseDto;
import com.testpire.testpire.dto.response.AvailableTestResponseDto;
import com.testpire.testpire.dto.response.TestAttemptResponseDto;
import com.testpire.testpire.entity.User;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.service.TestAttemptService;
import com.testpire.testpire.service.TestResolutionService;
import com.testpire.testpire.service.UserService;
import com.testpire.testpire.util.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Student-facing test-taking API. The acting student is always resolved from the JWT (never from the
 * request body), so a student can only ever see and act on their own attempts.
 */
@RestController
@RequestMapping("/api/student/tests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test Taking", description = "Student endpoints to view available tests and take them")
@SecurityRequirement(name = "Bearer Authentication")
public class TestAttemptController {

    private final TestAttemptService attemptService;
    private final TestResolutionService resolutionService;
    private final UserService userService;

    @GetMapping("/available")
    @RequirePermission(Permission.TEST_TAKE)
    @Operation(summary = "List available tests", description = "Tests the current student may take right now")
    public ResponseEntity<ApiResponseDto> getAvailableTests() {
        try {
            User student = currentStudent();
            List<AvailableTestResponseDto> tests =
                    resolutionService.getAvailableTestsForStudent(student.getId(), student.getInstituteId());
            return ResponseEntity.ok(ApiResponseDto.success("Available tests retrieved successfully", tests));
        } catch (Exception e) {
            log.error("Error listing available tests", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to list available tests: " + e.getMessage()));
        }
    }

    @PostMapping("/{testId}/attempts")
    @RequirePermission(Permission.TEST_TAKE)
    @Operation(summary = "Start (or resume) an attempt",
            description = "Starts a timed attempt, or resumes the in-progress one if it exists")
    public ResponseEntity<ApiResponseDto> startAttempt(
            @Parameter(description = "Test ID", required = true) @PathVariable Long testId) {
        try {
            User student = currentStudent();
            TestAttemptResponseDto attempt =
                    attemptService.startAttempt(testId, student.getId(), student.getInstituteId());
            return ResponseEntity.ok(ApiResponseDto.success("Attempt started", attempt));
        } catch (Exception e) {
            log.error("Error starting attempt", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to start attempt: " + e.getMessage()));
        }
    }

    @PutMapping("/attempts/{attemptId}/answers")
    @RequirePermission(Permission.TEST_TAKE)
    @Operation(summary = "Save an answer", description = "Upsert the student's answer to one question (while in progress)")
    public ResponseEntity<ApiResponseDto> saveAnswer(
            @Parameter(description = "Attempt ID", required = true) @PathVariable Long attemptId,
            @Valid @RequestBody SubmitAnswerRequestDto request) {
        try {
            User student = currentStudent();
            attemptService.saveAnswer(attemptId, student.getId(), request);
            return ResponseEntity.ok(ApiResponseDto.success("Answer saved", null));
        } catch (Exception e) {
            log.error("Error saving answer", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to save answer: " + e.getMessage()));
        }
    }

    @PostMapping("/attempts/{attemptId}/submit")
    @RequirePermission(Permission.TEST_TAKE)
    @Operation(summary = "Submit an attempt", description = "Submit for grading; may include any remaining answers")
    public ResponseEntity<ApiResponseDto> submit(
            @Parameter(description = "Attempt ID", required = true) @PathVariable Long attemptId,
            @Valid @RequestBody(required = false) SubmitAttemptRequestDto request) {
        try {
            User student = currentStudent();
            List<SubmitAnswerRequestDto> answers = request == null ? null : request.answers();
            TestAttemptResponseDto attempt = attemptService.submit(attemptId, student.getId(), answers);
            return ResponseEntity.ok(ApiResponseDto.success("Attempt submitted", attempt));
        } catch (Exception e) {
            log.error("Error submitting attempt", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to submit attempt: " + e.getMessage()));
        }
    }

    @GetMapping("/attempts")
    @RequirePermission(Permission.TEST_ATTEMPT_READ)
    @Operation(summary = "List my attempts",
            description = "The calling student's own attempt history (backs the Results tab); includes graded attempts even after the assignment window closes")
    public ResponseEntity<ApiResponseDto> getMyAttempts() {
        try {
            User student = currentStudent();
            List<AttemptSummaryResponseDto> attempts = attemptService.listOwnAttempts(student.getId());
            return ResponseEntity.ok(ApiResponseDto.success("Attempts retrieved", attempts));
        } catch (Exception e) {
            log.error("Error listing attempts", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to list attempts: " + e.getMessage()));
        }
    }

    @GetMapping("/attempts/{attemptId}")
    @RequirePermission(Permission.TEST_ATTEMPT_READ)
    @Operation(summary = "View an attempt", description = "The student's own attempt and (once graded) result")
    public ResponseEntity<ApiResponseDto> getAttempt(
            @Parameter(description = "Attempt ID", required = true) @PathVariable Long attemptId) {
        try {
            User student = currentStudent();
            TestAttemptResponseDto attempt = attemptService.getAttempt(attemptId, student.getId());
            return ResponseEntity.ok(ApiResponseDto.success("Attempt retrieved", attempt));
        } catch (Exception e) {
            log.error("Error getting attempt", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get attempt: " + e.getMessage()));
        }
    }

    /**
     * Resolves the authenticated student to the local {@code users} row.
     *
     * <p>{@link RequestUtils#getCurrentUsername()} returns the JWT principal, which is the
     * Cognito {@code cognito:username}/{@code sub} (a UUID) — NOT the local {@code username}
     * column (which holds the email). So we must look up by {@code cognito_user_id}, exactly
     * as {@code /api/students/profile} and {@code /peers} do; looking up by username here was a
     * bug that made every {@code /api/student/tests/*} call fail with "User not found".</p>
     */
    private User currentStudent() {
        String username = RequestUtils.getCurrentUsername();
        if (username == null) {
            throw new IllegalStateException("No authenticated user on the request");
        }
        return userService.getUserByCognitoUserId(username);
    }
}
