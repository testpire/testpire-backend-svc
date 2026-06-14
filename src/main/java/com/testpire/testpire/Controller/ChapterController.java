package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequirePermission;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.dto.request.ChapterCriteriaDto;
import com.testpire.testpire.dto.request.ChapterSearchRequestDto;
import com.testpire.testpire.dto.request.CreateChapterRequestDto;
import com.testpire.testpire.dto.request.PaginationRequestDto;
import com.testpire.testpire.dto.request.SortingRequestDto;
import com.testpire.testpire.dto.request.UpdateChapterRequestDto;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.dto.response.ChapterListResponseDto;
import com.testpire.testpire.dto.response.ChapterResponseDto;
import com.testpire.testpire.service.ChapterService;
import com.testpire.testpire.util.JwksJwtUtil;
import com.testpire.testpire.util.IncludeUtils;
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
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chapter Management", description = "APIs for managing chapters within subjects")
@SecurityRequirement(name = "Bearer Authentication")
public class ChapterController {

    private final ChapterService chapterService;
    private final JwksJwtUtil jwtUtil;

    @PostMapping
    @RequirePermission(Permission.CHAPTER_CREATE)
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
            Long instituteId = RequestUtils.resolveInstituteId(request.instituteId());
            log.info("Creating chapter: {} for subject: {} in institute: {}",
                    request.name(), request.subjectId(), instituteId);

            CreateChapterRequestDto requestWithInstituteId = new CreateChapterRequestDto(
                    request.name(),
                    request.description(),
                    request.code(),
                    request.subjectId(),
                    instituteId,
                    request.orderIndex(),
                    request.duration(),
                    request.objectives()
            );
            
            ChapterResponseDto chapter = chapterService.createChapter(requestWithInstituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Chapter created successfully", chapter));
        } catch (Exception e) {
            log.error("Error creating chapter", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to create chapter: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequirePermission(Permission.CHAPTER_UPDATE)
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
    @RequirePermission(Permission.CHAPTER_DELETE)
    @Operation(
        summary = "Delete a chapter",
        description = "Permanently deletes a chapter by ID. Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can delete chapters."
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
    @RequirePermission(Permission.CHAPTER_READ)
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
            @PathVariable Long id,
            @Parameter(description = "Comma-separated children to expand: topics", example = "topics")
            @RequestParam(required = false) String include) {
        try {
            log.info("Getting chapter with ID: {}", id);
            ChapterResponseDto chapter = chapterService.getChapterById(id, IncludeUtils.parse(include));
            return ResponseEntity.ok(ApiResponseDto.success("Chapter retrieved successfully", chapter));
        } catch (Exception e) {
            log.error("Error getting chapter", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get chapter: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{code}")
    @RequirePermission(Permission.CHAPTER_READ)
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
            @Parameter(description = "Comma-separated children to expand: topics", example = "topics")
            @RequestParam(required = false) String include) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Getting chapter with code: {} for institute: {}", code, instituteId);
            ChapterResponseDto chapter = chapterService.getChapterByCode(code, instituteId, IncludeUtils.parse(include));
            return ResponseEntity.ok(ApiResponseDto.success("Chapter retrieved successfully", chapter));
        } catch (Exception e) {
            log.error("Error getting chapter by code", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get chapter: " + e.getMessage()));
        }
    }

    @PostMapping("/search/advanced")
    @RequirePermission(Permission.CHAPTER_READ)
    @Operation(
        summary = "Advanced search for chapters",
        description = "Performs advanced search for chapters using multiple criteria including name, code, description, order index, duration, and more. Supports pagination and sorting. All authenticated users can search chapters."
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
    public ResponseEntity<ApiResponseDto> searchChaptersAdvanced(
            @Valid @RequestBody ChapterSearchRequestDto request,
            @Parameter(description = "Comma-separated children to expand: topics", example = "topics")
            @RequestParam(required = false) String include) {
        try {
            Long instituteId = RequestUtils.resolveInstituteId(request.getInstituteId());
            log.info("Advanced search for chapters with criteria: {}", request);
            
            // Set instituteId from JWT token
            ChapterCriteriaDto criteria = ChapterCriteriaDto.builder()
                    .instituteId(instituteId)
                    .subjectId(request.getSubjectId())
                    .searchText(request.getSearchText())
                    .name(request.getName())
                    .code(request.getCode())
                    .description(request.getDescription())
                    .minOrderIndex(request.getMinOrderIndex())
                    .maxOrderIndex(request.getMaxOrderIndex())
                    .minDuration(request.getMinDuration())
                    .maxDuration(request.getMaxDuration())
                    .objectives(request.getObjectives())
                    .hasTopics(request.getHasTopics())
                    .minTopics(request.getMinTopics())
                    .maxTopics(request.getMaxTopics())
                    .createdAfter(request.getCreatedAfter())
                    .createdBefore(request.getCreatedBefore())
                    .createdBy(request.getCreatedBy())
                    .build();
                    
            ChapterSearchRequestDto requestWithInstituteId = ChapterSearchRequestDto.builder()
                    .criteria(criteria)
                    .pagination(request.getPagination())
                    .sorting(request.getSorting())
                    .build();
                    
            ChapterListResponseDto chapters = chapterService.searchChaptersWithSpecification(requestWithInstituteId, IncludeUtils.parse(include));
            return ResponseEntity.ok(ApiResponseDto.success("Chapters retrieved successfully", chapters));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search chapters: " + e.getMessage()));
        }
    }

    @GetMapping("/search/advanced")
    @RequirePermission(Permission.CHAPTER_READ)
    @Operation(
        summary = "Advanced search for chapters (GET)",
        description = "Performs advanced search for chapters using query parameters. Supports filtering by name, code, description, order index, duration, and more. Supports pagination and sorting. All authenticated users can search chapters."
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
    public ResponseEntity<ApiResponseDto> searchChaptersAdvancedGet(
            @Parameter(description = "Subject ID (optional)", example = "1")
            @RequestParam(required = false) Long subjectId,
            @Parameter(description = "Search text (optional)", example = "Arrays")
            @RequestParam(required = false) String searchText,
            @Parameter(description = "Name (optional)", example = "Arrays and Linked Lists")
            @RequestParam(required = false) String name,
            @Parameter(description = "Code (optional)", example = "CH01")
            @RequestParam(required = false) String code,
            @Parameter(description = "Description (optional)", example = "Introduction to arrays")
            @RequestParam(required = false) String description,
            @Parameter(description = "Minimum order index (optional)", example = "1")
            @RequestParam(required = false) Integer minOrderIndex,
            @Parameter(description = "Maximum order index (optional)", example = "10")
            @RequestParam(required = false) Integer maxOrderIndex,
            @Parameter(description = "Minimum duration (optional)", example = "30")
            @RequestParam(required = false) Integer minDuration,
            @Parameter(description = "Maximum duration (optional)", example = "120")
            @RequestParam(required = false) Integer maxDuration,
            @Parameter(description = "Objectives (optional)", example = "Learn arrays")
            @RequestParam(required = false) String objectives,
            @Parameter(description = "Has topics (optional)", example = "true")
            @RequestParam(required = false) Boolean hasTopics,
            @Parameter(description = "Minimum topics (optional)", example = "1")
            @RequestParam(required = false) Integer minTopics,
            @Parameter(description = "Maximum topics (optional)", example = "10")
            @RequestParam(required = false) Integer maxTopics,
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
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @Parameter(description = "Comma-separated children to expand: topics", example = "topics")
            @RequestParam(required = false) String include) {
        try {
            // No instituteId GET param; non-SA scoped to JWT, SA honors X-Institute-Id header.
            Long instituteId = RequestUtils.resolveInstituteId(null);
            log.info("Advanced search for chapters with GET parameters");
            
            // Parse date strings to LocalDateTime
            Instant parsedCreatedAfter = null;
            Instant parsedCreatedBefore = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            if (createdAfter != null && !createdAfter.trim().isEmpty()) {
                try {
                    parsedCreatedAfter = LocalDateTime.parse(createdAfter + " 00:00:00", formatter).toInstant(ZoneOffset.UTC);
                } catch (Exception e) {
                    log.warn("Invalid createdAfter date format: {}", createdAfter);
                }
            }
            
            if (createdBefore != null && !createdBefore.trim().isEmpty()) {
                try {
                    parsedCreatedBefore = LocalDateTime.parse(createdBefore + " 23:59:59", formatter).toInstant(ZoneOffset.UTC);
                } catch (Exception e) {
                    log.warn("Invalid createdBefore date format: {}", createdBefore);
                }
            }
            
            ChapterCriteriaDto criteria = ChapterCriteriaDto.builder()
                    .instituteId(instituteId)
                    .subjectId(subjectId)
                    .searchText(searchText)
                    .name(name)
                    .code(code)
                    .description(description)
                    .minOrderIndex(minOrderIndex)
                    .maxOrderIndex(maxOrderIndex)
                    .minDuration(minDuration)
                    .maxDuration(maxDuration)
                    .objectives(objectives)
                    .hasTopics(hasTopics)
                    .minTopics(minTopics)
                    .maxTopics(maxTopics)
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
                    
            ChapterSearchRequestDto request = ChapterSearchRequestDto.builder()
                    .criteria(criteria)
                    .pagination(pagination)
                    .sorting(sorting)
                    .build();
            
            ChapterListResponseDto chapters = chapterService.searchChaptersWithSpecification(request, IncludeUtils.parse(include));
            return ResponseEntity.ok(ApiResponseDto.success("Chapters retrieved successfully", chapters));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search chapters: " + e.getMessage()));
        }
    }

}


