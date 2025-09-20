package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.request.CreateSubjectRequestDto;
import com.testpire.testpire.dto.request.UpdateSubjectRequestDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.dto.response.SubjectListResponseDto;
import com.testpire.testpire.dto.response.SubjectResponseDto;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.SubjectService;
import com.testpire.testpire.util.JwksJwtUtil;
import com.testpire.testpire.util.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subject")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subject Management", description = "APIs for managing subjects within courses")
@SecurityRequirement(name = "Bearer Authentication")
public class SubjectController {

    private final SubjectService subjectService;
    private final JwksJwtUtil jwtUtil;

    @PostMapping
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Create a new subject",
        description = "Creates a new subject for a specific course within an institute. Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can create subjects."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Subject created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Subject created successfully\", \"success\": true, \"data\": {\"id\": 1, \"name\": \"Data Structures\", \"code\": \"DS101\", \"courseId\": 1, \"instituteId\": 1}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation failed or subject code already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Subject with code DS101 already exists in this institute\", \"success\": false, \"data\": null}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> createSubject(@Valid @RequestBody CreateSubjectRequestDto request) {
        try {
            log.info("Creating subject: {} for course: {} in institute: {}", 
                    request.name(), request.courseId(), request.instituteId());
            SubjectResponseDto subject = subjectService.createSubject(request);
            return ResponseEntity.ok(ApiResponseDto.success("Subject created successfully", subject));
        } catch (Exception e) {
            log.error("Error creating subject", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to create subject: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Update an existing subject",
        description = "Updates an existing subject by ID. Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can update subjects."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Subject updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation failed or subject not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> updateSubject(
            @Parameter(description = "Subject ID", required = true, example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody UpdateSubjectRequestDto request) {
        try {
            log.info("Updating subject with ID: {}", id);
            SubjectResponseDto subject = subjectService.updateSubject(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Subject updated successfully", subject));
        } catch (Exception e) {
            log.error("Error updating subject", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to update subject: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Delete a subject",
        description = "Soft deletes a subject by ID (sets active to false). Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can delete subjects."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Subject deleted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - subject not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> deleteSubject(
            @Parameter(description = "Subject ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            log.info("Deleting subject with ID: {}", id);
            subjectService.deleteSubject(id);
            return ResponseEntity.ok(ApiResponseDto.success("Subject deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting subject", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to delete subject: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get subject by ID",
        description = "Retrieves a subject by its ID. All authenticated users can view subjects."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Subject retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - subject not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> getSubjectById(
            @Parameter(description = "Subject ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            log.info("Getting subject with ID: {}", id);
            SubjectResponseDto subject = subjectService.getSubjectById(id);
            return ResponseEntity.ok(ApiResponseDto.success("Subject retrieved successfully", subject));
        } catch (Exception e) {
            log.error("Error getting subject", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get subject: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{code}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get subject by code",
        description = "Retrieves a subject by its code within a specific institute. All authenticated users can view subjects."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Subject retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - subject not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> getSubjectByCode(
            @Parameter(description = "Subject code", required = true, example = "DS101")
            @PathVariable String code, 
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId) {
        try {
            log.info("Getting subject with code: {} for institute: {}", code, instituteId);
            SubjectResponseDto subject = subjectService.getSubjectByCode(code, instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Subject retrieved successfully", subject));
        } catch (Exception e) {
            log.error("Error getting subject by code", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get subject: " + e.getMessage()));
        }
    }

    @GetMapping("/institute/{instituteId}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get subjects by institute",
        description = "Retrieves all active subjects for a specific institute. All authenticated users can view subjects."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Subjects retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> getSubjectsByInstitute(
            @Parameter(description = "Institute ID", required = true, example = "1")
            @PathVariable Long instituteId) {
        try {
            log.info("Getting subjects for institute: {}", instituteId);
            SubjectListResponseDto subjects = subjectService.getSubjectsByInstitute(instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Subjects retrieved successfully", subjects));
        } catch (Exception e) {
            log.error("Error getting subjects by institute", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get subjects: " + e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get subjects by course",
        description = "Retrieves all active subjects for a specific course within an institute. All authenticated users can view subjects."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Subjects retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> getSubjectsByCourse(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long courseId, 
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId) {
        try {
            log.info("Getting subjects for course: {} in institute: {}", courseId, instituteId);
            SubjectListResponseDto subjects = subjectService.getSubjectsByCourse(courseId, instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Subjects retrieved successfully", subjects));
        } catch (Exception e) {
            log.error("Error getting subjects by course", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get subjects: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Search subjects",
        description = "Searches for subjects by name or code within a specific institute. All authenticated users can search subjects."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Subjects retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> searchSubjects(
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId, 
            @Parameter(description = "Search query", required = true, example = "Data Structures")
            @RequestParam String query) {
        try {
            log.info("Searching subjects in institute: {} with query: {}", instituteId, query);
            SubjectListResponseDto subjects = subjectService.searchSubjects(instituteId, query);
            return ResponseEntity.ok(ApiResponseDto.success("Subjects retrieved successfully", subjects));
        } catch (Exception e) {
            log.error("Error searching subjects", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search subjects: " + e.getMessage()));
        }
    }

    @GetMapping
    @RequireRole({UserRole.SUPER_ADMIN})
    @Operation(
        summary = "Get all subjects",
        description = "Retrieves all subjects across all institutes. Only SUPER_ADMIN users can access this endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Subjects retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> getAllSubjects() {
        try {
            log.info("Getting all subjects");
            SubjectListResponseDto subjects = subjectService.getAllSubjects();
            return ResponseEntity.ok(ApiResponseDto.success("Subjects retrieved successfully", subjects));
        } catch (Exception e) {
            log.error("Error getting all subjects", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get subjects: " + e.getMessage()));
        }
    }
}
