package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.request.CreateTopicRequestDto;
import com.testpire.testpire.dto.request.TopicCriteriaDto;
import com.testpire.testpire.dto.request.TopicSearchRequestDto;
import com.testpire.testpire.dto.request.PaginationRequestDto;
import com.testpire.testpire.dto.request.SortingRequestDto;
import com.testpire.testpire.dto.request.UpdateTopicRequestDto;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.dto.response.TopicListResponseDto;
import com.testpire.testpire.dto.response.TopicResponseDto;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.TopicService;
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
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Topic Management", description = "APIs for managing topics within chapters")
@SecurityRequirement(name = "Bearer Authentication")
public class TopicController {

    private final TopicService topicService;
    private final JwksJwtUtil jwtUtil;

    @PostMapping
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Create a new topic",
        description = "Creates a new topic for a specific chapter within an institute. Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can create topics."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Topic created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Topic created successfully\", \"success\": true, \"data\": {\"id\": 1, \"name\": \"Introduction to Arrays\", \"code\": \"T01\", \"chapterId\": 1, \"instituteId\": 1}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation failed or topic code already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Topic with code T01 already exists in this institute\", \"success\": false, \"data\": null}"
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
    public ResponseEntity<ApiResponseDto> createTopic(@Valid @RequestBody CreateTopicRequestDto request) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Creating topic: {} for chapter: {} in institute: {}", 
                    request.name(), request.chapterId(), instituteId);
            
            // Create a new request with instituteId from JWT
            CreateTopicRequestDto requestWithInstituteId = new CreateTopicRequestDto(
                    request.name(),
                    request.description(),
                    request.code(),
                    request.chapterId(),
                    instituteId,
                    request.orderIndex(),
                    request.duration(),
                    request.content(),
                    request.learningOutcomes()
            );
            
            TopicResponseDto topic = topicService.createTopic(requestWithInstituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Topic created successfully", topic));
        } catch (Exception e) {
            log.error("Error creating topic", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to create topic: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Update an existing topic",
        description = "Updates an existing topic by ID. Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can update topics."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Topic updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation failed or topic not found",
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
    public ResponseEntity<ApiResponseDto> updateTopic(
            @Parameter(description = "Topic ID", required = true, example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody UpdateTopicRequestDto request) {
        try {
            log.info("Updating topic with ID: {}", id);
            TopicResponseDto topic = topicService.updateTopic(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Topic updated successfully", topic));
        } catch (Exception e) {
            log.error("Error updating topic", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to update topic: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Delete a topic",
        description = "Soft deletes a topic by ID (sets active to false). Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can delete topics."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Topic deleted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - topic not found",
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
    public ResponseEntity<ApiResponseDto> deleteTopic(
            @Parameter(description = "Topic ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            log.info("Deleting topic with ID: {}", id);
            topicService.deleteTopic(id);
            return ResponseEntity.ok(ApiResponseDto.success("Topic deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting topic", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to delete topic: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get topic by ID",
        description = "Retrieves a topic by its ID. All authenticated users can view topics."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Topic retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - topic not found",
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
    public ResponseEntity<ApiResponseDto> getTopicById(
            @Parameter(description = "Topic ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            log.info("Getting topic with ID: {}", id);
            TopicResponseDto topic = topicService.getTopicById(id);
            return ResponseEntity.ok(ApiResponseDto.success("Topic retrieved successfully", topic));
        } catch (Exception e) {
            log.error("Error getting topic", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get topic: " + e.getMessage()));
        }
    }

    @GetMapping("/code/{code}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get topic by code",
        description = "Retrieves a topic by its code within a specific institute. All authenticated users can view topics."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Topic retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - topic not found",
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
    public ResponseEntity<ApiResponseDto> getTopicByCode(
            @Parameter(description = "Topic code", required = true, example = "T01")
            @PathVariable String code) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Getting topic with code: {} for institute: {}", code, instituteId);
            TopicResponseDto topic = topicService.getTopicByCode(code, instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Topic retrieved successfully", topic));
        } catch (Exception e) {
            log.error("Error getting topic by code", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get topic: " + e.getMessage()));
        }
    }

    @PostMapping("/search/advanced")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Advanced search for topics",
        description = "Performs advanced search for topics using multiple criteria including name, code, description, order index, duration, content, and more. Supports pagination and sorting. All authenticated users can search topics."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Topics retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> searchTopicsAdvanced(@Valid @RequestBody TopicSearchRequestDto request) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Advanced search for topics with criteria: {}", request);
            
            // Set instituteId from JWT token
            TopicCriteriaDto criteria = TopicCriteriaDto.builder()
                    .instituteId(instituteId)
                    .courseId(request.getCourseId())
                    .subjectId(request.getSubjectId())
                    .chapterId(request.getChapterId())
                    .searchText(request.getSearchText())
                    .name(request.getName())
                    .code(request.getCode())
                    .description(request.getDescription())
                    .content(request.getContent())
                    .learningOutcomes(request.getLearningOutcomes())
                    .minOrderIndex(request.getMinOrderIndex())
                    .maxOrderIndex(request.getMaxOrderIndex())
                    .minDuration(request.getMinDuration())
                    .maxDuration(request.getMaxDuration())
                    .active(request.getActive())
                    .hasQuestions(request.getHasQuestions())
                    .minQuestions(request.getMinQuestions())
                    .maxQuestions(request.getMaxQuestions())
                    .createdAfter(request.getCreatedAfter())
                    .createdBefore(request.getCreatedBefore())
                    .createdBy(request.getCreatedBy())
                    .build();
                    
            TopicSearchRequestDto requestWithInstituteId = TopicSearchRequestDto.builder()
                    .criteria(criteria)
                    .pagination(request.getPagination())
                    .sorting(request.getSorting())
                    .build();
                    
            TopicListResponseDto topics = topicService.searchTopicsWithSpecification(requestWithInstituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Topics retrieved successfully", topics));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search topics: " + e.getMessage()));
        }
    }

    @GetMapping("/search/advanced")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Advanced search for topics (GET)",
        description = "Performs advanced search for topics using query parameters. Supports filtering by name, code, description, order index, duration, content, and more. Supports pagination and sorting. All authenticated users can search topics."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Topics retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> searchTopicsAdvancedGet(
            @Parameter(description = "Course ID (optional)", example = "1")
            @RequestParam(required = false) Long courseId,
            @Parameter(description = "Subject ID (optional)", example = "1")
            @RequestParam(required = false) Long subjectId,
            @Parameter(description = "Chapter ID (optional)", example = "1")
            @RequestParam(required = false) Long chapterId,
            @Parameter(description = "Search text (optional)", example = "Arrays")
            @RequestParam(required = false) String searchText,
            @Parameter(description = "Name (optional)", example = "Introduction to Arrays")
            @RequestParam(required = false) String name,
            @Parameter(description = "Code (optional)", example = "T01")
            @RequestParam(required = false) String code,
            @Parameter(description = "Description (optional)", example = "Learn about arrays")
            @RequestParam(required = false) String description,
            @Parameter(description = "Content (optional)", example = "Array basics")
            @RequestParam(required = false) String content,
            @Parameter(description = "Learning outcomes (optional)", example = "Understand arrays")
            @RequestParam(required = false) String learningOutcomes,
            @Parameter(description = "Minimum order index (optional)", example = "1")
            @RequestParam(required = false) Integer minOrderIndex,
            @Parameter(description = "Maximum order index (optional)", example = "10")
            @RequestParam(required = false) Integer maxOrderIndex,
            @Parameter(description = "Minimum duration (optional)", example = "30")
            @RequestParam(required = false) Integer minDuration,
            @Parameter(description = "Maximum duration (optional)", example = "120")
            @RequestParam(required = false) Integer maxDuration,
            @Parameter(description = "Active status (optional)", example = "true")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "Has questions (optional)", example = "true")
            @RequestParam(required = false) Boolean hasQuestions,
            @Parameter(description = "Minimum questions (optional)", example = "1")
            @RequestParam(required = false) Integer minQuestions,
            @Parameter(description = "Maximum questions (optional)", example = "10")
            @RequestParam(required = false) Integer maxQuestions,
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
            log.info("Advanced search for topics with GET parameters");
            
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
            
            TopicCriteriaDto criteria = TopicCriteriaDto.builder()
                    .instituteId(instituteId)
                    .courseId(courseId)
                    .subjectId(subjectId)
                    .chapterId(chapterId)
                    .searchText(searchText)
                    .name(name)
                    .code(code)
                    .description(description)
                    .content(content)
                    .learningOutcomes(learningOutcomes)
                    .minOrderIndex(minOrderIndex)
                    .maxOrderIndex(maxOrderIndex)
                    .minDuration(minDuration)
                    .maxDuration(maxDuration)
                    .active(active)
                    .hasQuestions(hasQuestions)
                    .minQuestions(minQuestions)
                    .maxQuestions(maxQuestions)
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
                    
            TopicSearchRequestDto request = TopicSearchRequestDto.builder()
                    .criteria(criteria)
                    .pagination(pagination)
                    .sorting(sorting)
                    .build();
            
            TopicListResponseDto topics = topicService.searchTopicsWithSpecification(request);
            return ResponseEntity.ok(ApiResponseDto.success("Topics retrieved successfully", topics));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search topics: " + e.getMessage()));
        }
    }

}
