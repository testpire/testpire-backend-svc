package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.request.CreateSubjectRequestDto;
import com.testpire.testpire.dto.request.SubjectCriteriaDto;
import com.testpire.testpire.dto.request.SubjectSearchRequestDto;
import com.testpire.testpire.dto.request.PaginationRequestDto;
import com.testpire.testpire.dto.request.SortingRequestDto;
import com.testpire.testpire.dto.request.UpdateSubjectRequestDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@RequestMapping("/api/subjects")
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
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Creating subject: {} for course: {} in institute: {}", 
                    request.name(), request.courseId(), instituteId);
            
            // Create a new request with instituteId from JWT
            CreateSubjectRequestDto requestWithInstituteId = new CreateSubjectRequestDto(
                    request.name(),
                    request.description(),
                    request.code(),
                    request.courseId(),
                    instituteId,
                    request.duration(),
                    request.credits(),
                    request.prerequisites()
            );
            
            SubjectResponseDto subject = subjectService.createSubject(requestWithInstituteId);
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
            @PathVariable String code) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Getting subject with code: {} for institute: {}", code, instituteId);
            SubjectResponseDto subject = subjectService.getSubjectByCode(code, instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Subject retrieved successfully", subject));
        } catch (Exception e) {
            log.error("Error getting subject by code", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get subject: " + e.getMessage()));
        }
    }

    @PostMapping("/search/advanced")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Advanced search for subjects",
        description = "Performs advanced search for subjects using multiple criteria including name, code, description, duration, credits, and more. Supports pagination and sorting. All authenticated users can search subjects."
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
            responseCode = "400",
            description = "Bad request - validation failed",
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
    public ResponseEntity<ApiResponseDto> searchSubjectsAdvanced(@Valid @RequestBody SubjectSearchRequestDto request) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Advanced search for subjects with criteria: {}", request);
            
            // Set instituteId from JWT token
            SubjectCriteriaDto criteria = SubjectCriteriaDto.builder()
                    .instituteId(instituteId)
                    .courseId(request.getCourseId())
                    .searchText(request.getSearchText())
                    .name(request.getName())
                    .code(request.getCode())
                    .description(request.getDescription())
                    .minDuration(request.getMinDuration())
                    .maxDuration(request.getMaxDuration())
                    .minCredits(request.getMinCredits())
                    .maxCredits(request.getMaxCredits())
                    .prerequisites(request.getPrerequisites())
                    .active(request.getActive())
                    .hasChapters(request.getHasChapters())
                    .minChapters(request.getMinChapters())
                    .maxChapters(request.getMaxChapters())
                    .createdAfter(request.getCreatedAfter())
                    .createdBefore(request.getCreatedBefore())
                    .createdBy(request.getCreatedBy())
                    .build();
                    
            SubjectSearchRequestDto requestWithInstituteId = SubjectSearchRequestDto.builder()
                    .criteria(criteria)
                    .pagination(request.getPagination())
                    .sorting(request.getSorting())
                    .build();
                    
            SubjectListResponseDto subjects = subjectService.searchSubjectsWithSpecification(requestWithInstituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Subjects retrieved successfully", subjects));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search subjects: " + e.getMessage()));
        }
    }

    @GetMapping("/search/advanced")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Advanced search for subjects (GET)",
        description = "Performs advanced search for subjects using query parameters. Supports filtering by name, code, description, duration, credits, and more. Supports pagination and sorting. All authenticated users can search subjects."
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
            responseCode = "400",
            description = "Bad request - validation failed",
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
    public ResponseEntity<ApiResponseDto> searchSubjectsAdvancedGet(
            @Parameter(description = "Course ID (optional)", example = "1")
            @RequestParam(required = false) Long courseId,
            @Parameter(description = "Search text (optional)", example = "Data Structures")
            @RequestParam(required = false) String searchText,
            @Parameter(description = "Name (optional)", example = "Data Structures")
            @RequestParam(required = false) String name,
            @Parameter(description = "Code (optional)", example = "DS101")
            @RequestParam(required = false) String code,
            @Parameter(description = "Description (optional)", example = "Introduction to data structures")
            @RequestParam(required = false) String description,
            @Parameter(description = "Minimum duration (optional)", example = "30")
            @RequestParam(required = false) Integer minDuration,
            @Parameter(description = "Maximum duration (optional)", example = "120")
            @RequestParam(required = false) Integer maxDuration,
            @Parameter(description = "Minimum credits (optional)", example = "3")
            @RequestParam(required = false) Integer minCredits,
            @Parameter(description = "Maximum credits (optional)", example = "6")
            @RequestParam(required = false) Integer maxCredits,
            @Parameter(description = "Prerequisites (optional)", example = "Basic programming")
            @RequestParam(required = false) String prerequisites,
            @Parameter(description = "Active status (optional)", example = "true")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Has chapters (optional)", example = "true")
            @RequestParam(required = false) Boolean hasChapters,
            @Parameter(description = "Minimum chapters (optional)", example = "1")
            @RequestParam(required = false) Integer minChapters,
            @Parameter(description = "Maximum chapters (optional)", example = "10")
            @RequestParam(required = false) Integer maxChapters,
            @Parameter(description = "Created after (optional)", example = "2024-01-01")
            @RequestParam(required = false) String createdAfter,
            @Parameter(description = "Created before (optional)", example = "2024-12-31")
            @RequestParam(required = false) String createdBefore,
            @Parameter(description = "Created by (optional)", example = "admin")
            @RequestParam(required = false) String createdBy,
            @Parameter(description = "Page number (optional)", example = "0")
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size (optional)", example = "20")
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @Parameter(description = "Sort by field (optional)", example = "createdAt")
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (optional)", example = "desc")
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Advanced search for subjects with GET parameters");
            
            // Parse date strings to LocalDateTime
            LocalDateTime parsedCreatedAfter = null;
            LocalDateTime parsedCreatedBefore = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            if (createdAfter != null && !createdAfter.trim().isEmpty()) {
                try {
                    parsedCreatedAfter = LocalDateTime.parse(createdAfter + " 00:00:00", formatter);
                } catch (Exception e) {
                    log.warn("Invalid createdAfter date format: {}", createdAfter);
                }
            }
            
            if (createdBefore != null && !createdBefore.trim().isEmpty()) {
                try {
                    parsedCreatedBefore = LocalDateTime.parse(createdBefore + " 23:59:59", formatter);
                } catch (Exception e) {
                    log.warn("Invalid createdBefore date format: {}", createdBefore);
                }
            }
            
            SubjectCriteriaDto criteria = SubjectCriteriaDto.builder()
                    .instituteId(instituteId)
                    .courseId(courseId)
                    .searchText(searchText)
                    .name(name)
                    .code(code)
                    .description(description)
                    .minDuration(minDuration)
                    .maxDuration(maxDuration)
                    .minCredits(minCredits)
                    .maxCredits(maxCredits)
                    .prerequisites(prerequisites)
                    .active(active)
                    .hasChapters(hasChapters)
                    .minChapters(minChapters)
                    .maxChapters(maxChapters)
                    .createdAfter(parsedCreatedAfter)
                    .createdBefore(parsedCreatedBefore)
                    .createdBy(createdBy)
                    .build();
                    
            PaginationRequestDto pagination = PaginationRequestDto.builder()
                    .page(page)
                    .size(size)
                    .build();
                    
            SortingRequestDto sorting = SortingRequestDto.builder()
                    .field(sortBy)
                    .direction(sortDirection)
                    .build();
                    
            SubjectSearchRequestDto request = SubjectSearchRequestDto.builder()
                    .criteria(criteria)
                    .pagination(pagination)
                    .sorting(sorting)
                    .build();
            
            SubjectListResponseDto subjects = subjectService.searchSubjectsWithSpecification(request);
            return ResponseEntity.ok(ApiResponseDto.success("Subjects retrieved successfully", subjects));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search subjects: " + e.getMessage()));
        }
    }

}



