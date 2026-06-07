package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequirePermission;
import com.testpire.testpire.dto.request.CreateBatchRequestDto;
import com.testpire.testpire.dto.request.UpdateBatchRequestDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.dto.response.BatchResponseDto;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.service.BatchService;
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
@RequestMapping("/api/batches")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Batch Management", description = "Manage batches (cohorts) under a course")
@SecurityRequirement(name = "Bearer Authentication")
public class BatchController {

    private final BatchService batchService;

    @PostMapping
    @RequirePermission(Permission.BATCH_CREATE)
    @Operation(summary = "Create a batch", description = "Create a batch under a course (TEACHER and above)")
    public ResponseEntity<ApiResponseDto> createBatch(@Valid @RequestBody CreateBatchRequestDto request) {
        try {
            BatchResponseDto batch = batchService.createBatch(request);
            return ResponseEntity.ok(ApiResponseDto.success("Batch created successfully", batch));
        } catch (Exception e) {
            log.error("Error creating batch", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to create batch: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequirePermission(Permission.BATCH_UPDATE)
    @Operation(summary = "Update a batch", description = "Update a batch (TEACHER and above)")
    public ResponseEntity<ApiResponseDto> updateBatch(
            @Parameter(description = "Batch ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateBatchRequestDto request) {
        try {
            BatchResponseDto batch = batchService.updateBatch(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Batch updated successfully", batch));
        } catch (Exception e) {
            log.error("Error updating batch", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to update batch: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequirePermission(Permission.BATCH_DELETE)
    @Operation(summary = "Delete a batch", description = "Soft-delete a batch (TEACHER and above)")
    public ResponseEntity<ApiResponseDto> deleteBatch(
            @Parameter(description = "Batch ID", required = true) @PathVariable Long id) {
        try {
            batchService.deleteBatch(id);
            return ResponseEntity.ok(ApiResponseDto.success("Batch deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting batch", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to delete batch: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequirePermission(Permission.BATCH_READ)
    @Operation(summary = "Get batch by ID", description = "Retrieve a batch by its ID")
    public ResponseEntity<ApiResponseDto> getBatchById(
            @Parameter(description = "Batch ID", required = true) @PathVariable Long id) {
        try {
            BatchResponseDto batch = batchService.getBatchById(id);
            return ResponseEntity.ok(ApiResponseDto.success("Batch retrieved successfully", batch));
        } catch (Exception e) {
            log.error("Error getting batch", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get batch: " + e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}")
    @RequirePermission(Permission.BATCH_READ)
    @Operation(summary = "List batches for a course", description = "List all batches under a given course")
    public ResponseEntity<ApiResponseDto> getBatchesByCourse(
            @Parameter(description = "Course ID", required = true) @PathVariable Long courseId) {
        try {
            List<BatchResponseDto> batches = batchService.getBatchesByCourse(courseId);
            return ResponseEntity.ok(ApiResponseDto.success("Batches retrieved successfully", batches));
        } catch (Exception e) {
            log.error("Error listing batches for course", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to list batches: " + e.getMessage()));
        }
    }
}
