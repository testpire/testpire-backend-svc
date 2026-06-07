package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequirePermission;
import com.testpire.testpire.dto.request.AssignTestRequestDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.dto.response.TestAssignmentResponseDto;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.service.TestAssignmentService;
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

@RestController
@RequestMapping("/api/tests/{testId}/assignments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test Assignment", description = "Assign a test to a course, batch, or student (dynamic resolution)")
@SecurityRequirement(name = "Bearer Authentication")
public class TestAssignmentController {

    private final TestAssignmentService assignmentService;

    @PostMapping
    @RequirePermission(Permission.TEST_ASSIGN)
    @Operation(summary = "Assign a test",
            description = "Assign a published test to a COURSE / BATCH / STUDENT. A course assignment reaches "
                    + "every student in every batch of the course, resolved dynamically at read time.")
    public ResponseEntity<ApiResponseDto> assign(
            @Parameter(description = "Test ID", required = true) @PathVariable Long testId,
            @Valid @RequestBody AssignTestRequestDto request) {
        try {
            TestAssignmentResponseDto assignment = assignmentService.assign(testId, request);
            return ResponseEntity.ok(ApiResponseDto.success("Test assigned successfully", assignment));
        } catch (Exception e) {
            log.error("Error assigning test", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to assign test: " + e.getMessage()));
        }
    }

    @GetMapping
    @RequirePermission(Permission.TEST_ASSIGN)
    @Operation(summary = "List assignments", description = "List all assignments for a test")
    public ResponseEntity<ApiResponseDto> listAssignments(
            @Parameter(description = "Test ID", required = true) @PathVariable Long testId) {
        try {
            List<TestAssignmentResponseDto> assignments = assignmentService.listAssignments(testId);
            return ResponseEntity.ok(ApiResponseDto.success("Assignments retrieved successfully", assignments));
        } catch (Exception e) {
            log.error("Error listing test assignments", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to list assignments: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{assignmentId}")
    @RequirePermission(Permission.TEST_ASSIGN)
    @Operation(summary = "Remove an assignment", description = "Unassign a test from a target")
    public ResponseEntity<ApiResponseDto> unassign(
            @Parameter(description = "Test ID", required = true) @PathVariable Long testId,
            @Parameter(description = "Assignment ID", required = true) @PathVariable Long assignmentId) {
        try {
            assignmentService.unassign(testId, assignmentId);
            return ResponseEntity.ok(ApiResponseDto.success("Assignment removed successfully", null));
        } catch (Exception e) {
            log.error("Error removing test assignment", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to remove assignment: " + e.getMessage()));
        }
    }
}
