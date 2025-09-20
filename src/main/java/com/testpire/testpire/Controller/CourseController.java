package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.request.CreateCourseRequestDto;
import com.testpire.testpire.dto.request.UpdateCourseRequestDto;
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
@RequestMapping("/api/course")
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
            log.info("Creating course: {} for institute: {}", request.name(), request.instituteId());
            CourseResponseDto course = courseService.createCourse(request);
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
            @PathVariable String code, 
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId) {
        try {
            log.info("Getting course with code: {} for institute: {}", code, instituteId);
            CourseResponseDto course = courseService.getCourseByCode(code, instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Course retrieved successfully", course));
        } catch (Exception e) {
            log.error("Error getting course by code", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get course: " + e.getMessage()));
        }
    }

    @GetMapping("/institute/{instituteId}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get courses by institute",
        description = "Retrieves all active courses for a specific institute. All authenticated users can view courses."
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
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> getCoursesByInstitute(
            @Parameter(description = "Institute ID", required = true, example = "1")
            @PathVariable Long instituteId) {
        try {
            log.info("Getting courses for institute: {}", instituteId);
            CourseListResponseDto courses = courseService.getCoursesByInstitute(instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Courses retrieved successfully", courses));
        } catch (Exception e) {
            log.error("Error getting courses by institute", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get courses: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Search courses",
        description = "Searches for courses by name or code within a specific institute. All authenticated users can search courses."
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
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> searchCourses(
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId, 
            @Parameter(description = "Search query", required = true, example = "Computer Science")
            @RequestParam String query) {
        try {
            log.info("Searching courses in institute: {} with query: {}", instituteId, query);
            CourseListResponseDto courses = courseService.searchCourses(instituteId, query);
            return ResponseEntity.ok(ApiResponseDto.success("Courses retrieved successfully", courses));
        } catch (Exception e) {
            log.error("Error searching courses", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search courses: " + e.getMessage()));
        }
    }

    @GetMapping
    @RequireRole({UserRole.SUPER_ADMIN})
    @Operation(
        summary = "Get all courses",
        description = "Retrieves all courses across all institutes. Only SUPER_ADMIN users can access this endpoint."
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
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> getAllCourses() {
        try {
            log.info("Getting all courses");
            CourseListResponseDto courses = courseService.getAllCourses();
            return ResponseEntity.ok(ApiResponseDto.success("Courses retrieved successfully", courses));
        } catch (Exception e) {
            log.error("Error getting all courses", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get courses: " + e.getMessage()));
        }
    }
}