package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequirePermission;
import com.testpire.testpire.dto.request.AddTestQuestionsRequestDto;
import com.testpire.testpire.dto.request.CreateTestRequestDto;
import com.testpire.testpire.dto.request.UpdateTestRequestDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.dto.response.TestResponseDto;
import com.testpire.testpire.dto.response.TestResultResponseDto;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.service.TestAttemptService;
import com.testpire.testpire.service.TestService;
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
@RequestMapping("/api/tests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test Management", description = "Create tests, curate their questions, publish, and view results (staff)")
@SecurityRequirement(name = "Bearer Authentication")
public class TestController {

    private final TestService testService;
    private final TestAttemptService testAttemptService;

    @PostMapping
    @RequirePermission(Permission.TEST_CREATE)
    @Operation(summary = "Create a test", description = "Create a test in DRAFT (TEACHER and above)")
    public ResponseEntity<ApiResponseDto> createTest(@Valid @RequestBody CreateTestRequestDto request) {
        try {
            TestResponseDto test = testService.createTest(request);
            return ResponseEntity.ok(ApiResponseDto.success("Test created successfully", test));
        } catch (Exception e) {
            log.error("Error creating test", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to create test: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequirePermission(Permission.TEST_UPDATE)
    @Operation(summary = "Update a test", description = "Update test metadata (TEACHER and above)")
    public ResponseEntity<ApiResponseDto> updateTest(
            @Parameter(description = "Test ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateTestRequestDto request) {
        try {
            TestResponseDto test = testService.updateTest(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Test updated successfully", test));
        } catch (Exception e) {
            log.error("Error updating test", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to update test: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequirePermission(Permission.TEST_DELETE)
    @Operation(summary = "Delete a test", description = "Soft-delete a test (TEACHER and above)")
    public ResponseEntity<ApiResponseDto> deleteTest(
            @Parameter(description = "Test ID", required = true) @PathVariable Long id) {
        try {
            testService.deleteTest(id);
            return ResponseEntity.ok(ApiResponseDto.success("Test deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting test", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to delete test: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequirePermission(Permission.TEST_READ)
    @Operation(summary = "Get test by ID", description = "Retrieve a test with its questions (staff view)")
    public ResponseEntity<ApiResponseDto> getTestById(
            @Parameter(description = "Test ID", required = true) @PathVariable Long id) {
        try {
            TestResponseDto test = testService.getTestById(id);
            return ResponseEntity.ok(ApiResponseDto.success("Test retrieved successfully", test));
        } catch (Exception e) {
            log.error("Error getting test", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get test: " + e.getMessage()));
        }
    }

    @GetMapping
    @RequirePermission(Permission.TEST_READ)
    @Operation(summary = "List tests", description = "List all tests in the caller's institute")
    public ResponseEntity<ApiResponseDto> listTests() {
        try {
            List<TestResponseDto> tests = testService.listTests();
            return ResponseEntity.ok(ApiResponseDto.success("Tests retrieved successfully", tests));
        } catch (Exception e) {
            log.error("Error listing tests", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to list tests: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/questions")
    @RequirePermission(Permission.TEST_UPDATE)
    @Operation(summary = "Add/update questions", description = "Add questions to a DRAFT test (upserts marks/order)")
    public ResponseEntity<ApiResponseDto> addQuestions(
            @Parameter(description = "Test ID", required = true) @PathVariable Long id,
            @Valid @RequestBody AddTestQuestionsRequestDto request) {
        try {
            TestResponseDto test = testService.addQuestions(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Questions updated successfully", test));
        } catch (Exception e) {
            log.error("Error adding questions to test", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to add questions: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    @RequirePermission(Permission.TEST_UPDATE)
    @Operation(summary = "Remove a question", description = "Remove a question from a DRAFT test")
    public ResponseEntity<ApiResponseDto> removeQuestion(
            @Parameter(description = "Test ID", required = true) @PathVariable Long id,
            @Parameter(description = "Question ID", required = true) @PathVariable Long questionId) {
        try {
            TestResponseDto test = testService.removeQuestion(id, questionId);
            return ResponseEntity.ok(ApiResponseDto.success("Question removed successfully", test));
        } catch (Exception e) {
            log.error("Error removing question from test", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to remove question: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/publish")
    @RequirePermission(Permission.TEST_PUBLISH)
    @Operation(summary = "Publish a test", description = "Move a test DRAFT -> PUBLISHED (requires at least one question)")
    public ResponseEntity<ApiResponseDto> publish(
            @Parameter(description = "Test ID", required = true) @PathVariable Long id) {
        try {
            TestResponseDto test = testService.publish(id);
            return ResponseEntity.ok(ApiResponseDto.success("Test published successfully", test));
        } catch (Exception e) {
            log.error("Error publishing test", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to publish test: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/results")
    @RequirePermission(Permission.TEST_RESULTS_READ)
    @Operation(summary = "View test results", description = "Every student's marks for the test (staff)")
    public ResponseEntity<ApiResponseDto> getResults(
            @Parameter(description = "Test ID", required = true) @PathVariable Long id) {
        try {
            TestResultResponseDto results = testAttemptService.getResults(id);
            return ResponseEntity.ok(ApiResponseDto.success("Results retrieved successfully", results));
        } catch (Exception e) {
            log.error("Error getting test results", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get results: " + e.getMessage()));
        }
    }
}
