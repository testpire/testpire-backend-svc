package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.request.CreateChapterRequestDto;
import com.testpire.testpire.dto.request.UpdateChapterRequestDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.dto.response.ChapterListResponseDto;
import com.testpire.testpire.dto.response.ChapterResponseDto;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.ChapterService;
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
@RequestMapping("/api/chapter")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chapter Management", description = "APIs for managing chapters within subjects")
@SecurityRequirement(name = "Bearer Authentication")
public class ChapterController {

    private final ChapterService chapterService;
    private final JwksJwtUtil jwtUtil;

    @PostMapping
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Create a new chapter",
        description = "Creates a new chapter for a specific subject within an institute. Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can create chapters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chapter created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Chapter created successfully\", \"success\": true, \"data\": {\"id\": 1, \"name\": \"Arrays and Linked Lists\", \"code\": \"CH01\", \"subjectId\": 1, \"instituteId\": 1}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation failed or chapter code already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Chapter with code CH01 already exists in this institute\", \"success\": false, \"data\": null}"
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
    public ResponseEntity<ApiResponseDto> createChapter(@Valid @RequestBody CreateChapterRequestDto request) {
        try {
            log.info("Creating chapter: {} for subject: {} in institute: {}", 
                    request.name(), request.subjectId(), request.instituteId());
            ChapterResponseDto chapter = chapterService.createChapter(request);
            return ResponseEntity.ok(ApiResponseDto.success("Chapter created successfully", chapter));
        } catch (Exception e) {
            log.error("Error creating chapter", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to create chapter: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Update an existing chapter",
        description = "Updates an existing chapter by ID. Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can update chapters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chapter updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation failed or chapter not found",
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
    public ResponseEntity<ApiResponseDto> updateChapter(
            @Parameter(description = "Chapter ID", required = true, example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody UpdateChapterRequestDto request) {
        try {
            log.info("Updating chapter with ID: {}", id);
            ChapterResponseDto chapter = chapterService.updateChapter(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Chapter updated successfully", chapter));
        } catch (Exception e) {
            log.error("Error updating chapter", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to update chapter: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Delete a chapter",
        description = "Soft deletes a chapter by ID (sets active to false). Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can delete chapters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chapter deleted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - chapter not found",
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
    public ResponseEntity<ApiResponseDto> deleteChapter(
            @Parameter(description = "Chapter ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            log.info("Deleting chapter with ID: {}", id);
            chapterService.deleteChapter(id);
            return ResponseEntity.ok(ApiResponseDto.success("Chapter deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting chapter", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to delete chapter: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get chapter by ID",
        description = "Retrieves a chapter by its ID. All authenticated users can view chapters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chapter retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - chapter not found",
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
    public ResponseEntity<ApiResponseDto> getChapterById(
            @Parameter(description = "Chapter ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            log.info("Getting chapter with ID: {}", id);
            ChapterResponseDto chapter = chapterService.getChapterById(id);
            return ResponseEntity.ok(ApiResponseDto.success("Chapter retrieved successfully", chapter));
        } catch (Exception e) {
            log.error("Error getting chapter", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get chapter: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{code}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get chapter by code",
        description = "Retrieves a chapter by its code within a specific institute. All authenticated users can view chapters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chapter retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - chapter not found",
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
    public ResponseEntity<ApiResponseDto> getChapterByCode(
            @Parameter(description = "Chapter code", required = true, example = "CH01")
            @PathVariable String code, 
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId) {
        try {
            log.info("Getting chapter with code: {} for institute: {}", code, instituteId);
            ChapterResponseDto chapter = chapterService.getChapterByCode(code, instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Chapter retrieved successfully", chapter));
        } catch (Exception e) {
            log.error("Error getting chapter by code", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get chapter: " + e.getMessage()));
        }
    }

    @GetMapping("/institute/{instituteId}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get chapters by institute",
        description = "Retrieves all active chapters for a specific institute. All authenticated users can view chapters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chapters retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> getChaptersByInstitute(
            @Parameter(description = "Institute ID", required = true, example = "1")
            @PathVariable Long instituteId) {
        try {
            log.info("Getting chapters for institute: {}", instituteId);
            ChapterListResponseDto chapters = chapterService.getChaptersByInstitute(instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Chapters retrieved successfully", chapters));
        } catch (Exception e) {
            log.error("Error getting chapters by institute", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get chapters: " + e.getMessage()));
        }
    }

    @GetMapping("/subject/{subjectId}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get chapters by subject",
        description = "Retrieves all active chapters for a specific subject within an institute. All authenticated users can view chapters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chapters retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> getChaptersBySubject(
            @Parameter(description = "Subject ID", required = true, example = "1")
            @PathVariable Long subjectId, 
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId) {
        try {
            log.info("Getting chapters for subject: {} in institute: {}", subjectId, instituteId);
            ChapterListResponseDto chapters = chapterService.getChaptersBySubject(subjectId, instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Chapters retrieved successfully", chapters));
        } catch (Exception e) {
            log.error("Error getting chapters by subject", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get chapters: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Search chapters",
        description = "Searches for chapters by name or code within a specific institute. All authenticated users can search chapters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chapters retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> searchChapters(
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId, 
            @Parameter(description = "Search query", required = true, example = "Arrays")
            @RequestParam String query) {
        try {
            log.info("Searching chapters in institute: {} with query: {}", instituteId, query);
            ChapterListResponseDto chapters = chapterService.searchChapters(instituteId, query);
            return ResponseEntity.ok(ApiResponseDto.success("Chapters retrieved successfully", chapters));
        } catch (Exception e) {
            log.error("Error searching chapters", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search chapters: " + e.getMessage()));
        }
    }

    @GetMapping
    @RequireRole({UserRole.SUPER_ADMIN})
    @Operation(
        summary = "Get all chapters",
        description = "Retrieves all chapters across all institutes. Only SUPER_ADMIN users can access this endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chapters retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> getAllChapters() {
        try {
            log.info("Getting all chapters");
            ChapterListResponseDto chapters = chapterService.getAllChapters();
            return ResponseEntity.ok(ApiResponseDto.success("Chapters retrieved successfully", chapters));
        } catch (Exception e) {
            log.error("Error getting all chapters", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get chapters: " + e.getMessage()));
        }
    }
}
