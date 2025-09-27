package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.request.CreateCourseRequestDto;
import com.testpire.testpire.dto.request.CourseCriteriaDto;
import com.testpire.testpire.dto.request.CourseSearchRequestDto;
import com.testpire.testpire.dto.request.PaginationRequestDto;
import com.testpire.testpire.dto.request.SortingRequestDto;
import com.testpire.testpire.dto.request.UpdateCourseRequestDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.dto.response.CourseListResponseDto;
import com.testpire.testpire.dto.response.CourseResponseDto;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.CourseService;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Course Management", description = "APIs for managing courses in the institute management system")
@SecurityRequirement(name = "Bearer Authentication")
public class CourseController {

    private final CourseService courseService;
    private final JwksJwtUtil jwtUtil;

    @PostMapping
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Create a new course",
        description = "Creates a new course for a specific institute. Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can create courses."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Course created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Course created successfully\", \"success\": true, \"data\": {\"id\": 1, \"name\": \"Computer Science\", \"code\": \"CS101\", \"instituteId\": 1}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation failed or course code already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Course with code CS101 already exists in this institute\", \"success\": false, \"data\": null}"
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
    public ResponseEntity<ApiResponseDto> createCourse(
            @Valid @RequestBody CreateCourseRequestDto request) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Creating course: {} for institute: {}", request.name(), instituteId);
            
            // Create a new request with instituteId from JWT
            CreateCourseRequestDto requestWithInstituteId = new CreateCourseRequestDto(
                    request.name(),
                    request.description(),
                    request.code(),
                    instituteId,
                    request.duration(),
                    request.level(),
                    request.prerequisites()
            );
                    
            CourseResponseDto course = courseService.createCourse(requestWithInstituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Course created successfully", course));
        } catch (Exception e) {
            log.error("Error creating course", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to create course: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Update an existing course",
        description = "Updates an existing course by ID. Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can update courses."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Course updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation failed or course not found",
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
    public ResponseEntity<ApiResponseDto> updateCourse(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody UpdateCourseRequestDto request) {
        try {
            log.info("Updating course with ID: {}", id);
            CourseResponseDto course = courseService.updateCourse(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Course updated successfully", course));
        } catch (Exception e) {
            log.error("Error updating course", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to update course: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Delete a course",
        description = "Soft deletes a course by ID (sets active to false). Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can delete courses."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Course deleted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - course not found",
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
    public ResponseEntity<ApiResponseDto> deleteCourse(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            log.info("Deleting course with ID: {}", id);
            courseService.deleteCourse(id);
            return ResponseEntity.ok(ApiResponseDto.success("Course deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting course", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to delete course: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get course by ID",
        description = "Retrieves a course by its ID. All authenticated users can view courses."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Course retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - course not found",
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
    public ResponseEntity<ApiResponseDto> getCourseById(
            @Parameter(description = "Course ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            log.info("Getting course with ID: {}", id);
            CourseResponseDto course = courseService.getCourseById(id);
            return ResponseEntity.ok(ApiResponseDto.success("Course retrieved successfully", course));
        } catch (Exception e) {
            log.error("Error getting course", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get course: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{code}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get course by code",
        description = "Retrieves a course by its code within a specific institute. All authenticated users can view courses."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Course retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - course not found",
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
    public ResponseEntity<ApiResponseDto> getCourseByCode(
            @Parameter(description = "Course code", required = true, example = "CS101")
            @PathVariable String code) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Getting course with code: {} for institute: {}", code, instituteId);
            CourseResponseDto course = courseService.getCourseByCode(code, instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Course retrieved successfully", course));
        } catch (Exception e) {
            log.error("Error getting course by code", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get course: " + e.getMessage()));
        }
    }

    @PostMapping("/search/advanced")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Advanced search for courses",
        description = "Performs advanced search for courses using multiple criteria including name, code, description, duration, level, and more. Supports pagination and sorting. All authenticated users can search courses."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Courses retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> searchCoursesAdvanced(@Valid @RequestBody CourseSearchRequestDto request) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Advanced search for courses with criteria: {}", request);
            request.getCriteria().setInstituteId(instituteId);
            CourseListResponseDto courses = courseService.searchCoursesWithSpecification(request);
            return ResponseEntity.ok(ApiResponseDto.success("Courses retrieved successfully", courses));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search courses: " + e.getMessage()));
        }
    }

    @GetMapping("/search/advanced")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Advanced search for courses (GET)",
        description = "Performs advanced search for courses using query parameters. Supports filtering by name, code, description, duration, level, and more. Supports pagination and sorting. All authenticated users can search courses."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Courses retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> searchCoursesAdvancedGet(
            @Parameter(description = "Search text (optional)", example = "Computer Science")
            @RequestParam(required = false) String searchText,
            @Parameter(description = "Name (optional)", example = "Computer Science")
            @RequestParam(required = false) String name,
            @Parameter(description = "Code (optional)", example = "CS101")
            @RequestParam(required = false) String code,
            @Parameter(description = "Description (optional)", example = "Introduction to programming")
            @RequestParam(required = false) String description,
            @Parameter(description = "Minimum duration (optional)", example = "30")
            @RequestParam(required = false) Integer minDuration,
            @Parameter(description = "Maximum duration (optional)", example = "120")
            @RequestParam(required = false) Integer maxDuration,
            @Parameter(description = "Level (optional)", example = "BEGINNER")
            @RequestParam(required = false) String level,
            @Parameter(description = "Prerequisites (optional)", example = "Basic math")
            @RequestParam(required = false) String prerequisites,
            @Parameter(description = "Active status (optional)", example = "true")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Has subjects (optional)", example = "true")
            @RequestParam(required = false) Boolean hasSubjects,
            @Parameter(description = "Minimum subjects (optional)", example = "1")
            @RequestParam(required = false) Integer minSubjects,
            @Parameter(description = "Maximum subjects (optional)", example = "10")
            @RequestParam(required = false) Integer maxSubjects,
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
            log.info("Advanced search for courses with GET parameters");
            
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
            
            CourseCriteriaDto criteria = CourseCriteriaDto.builder()
                    .instituteId(instituteId)
                    .searchText(searchText)
                    .name(name)
                    .code(code)
                    .description(description)
                    .minDuration(minDuration)
                    .maxDuration(maxDuration)
                    .level(level)
                    .prerequisites(prerequisites)
                    .active(active)
                    .hasSubjects(hasSubjects)
                    .minSubjects(minSubjects)
                    .maxSubjects(maxSubjects)
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
                    
            CourseSearchRequestDto request = CourseSearchRequestDto.builder()
                    .criteria(criteria)
                    .pagination(pagination)
                    .sorting(sorting)
                    .build();
            
            CourseListResponseDto courses = courseService.searchCoursesWithSpecification(request);
            return ResponseEntity.ok(ApiResponseDto.success("Courses retrieved successfully", courses));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search courses: " + e.getMessage()));
        }
    }

}