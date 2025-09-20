package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.request.CreateTopicRequestDto;
import com.testpire.testpire.dto.request.UpdateTopicRequestDto;
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
@RequestMapping("/api/topic")
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
            log.info("Creating topic: {} for chapter: {} in institute: {}", 
                    request.name(), request.chapterId(), request.instituteId());
            TopicResponseDto topic = topicService.createTopic(request);
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
            @PathVariable String code, 
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId) {
        try {
            log.info("Getting topic with code: {} for institute: {}", code, instituteId);
            TopicResponseDto topic = topicService.getTopicByCode(code, instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Topic retrieved successfully", topic));
        } catch (Exception e) {
            log.error("Error getting topic by code", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get topic: " + e.getMessage()));
        }
    }

    @GetMapping("/institute/{instituteId}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get topics by institute",
        description = "Retrieves all active topics for a specific institute. All authenticated users can view topics."
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
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> getTopicsByInstitute(
            @Parameter(description = "Institute ID", required = true, example = "1")
            @PathVariable Long instituteId) {
        try {
            log.info("Getting topics for institute: {}", instituteId);
            TopicListResponseDto topics = topicService.getTopicsByInstitute(instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Topics retrieved successfully", topics));
        } catch (Exception e) {
            log.error("Error getting topics by institute", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get topics: " + e.getMessage()));
        }
    }

    @GetMapping("/chapter/{chapterId}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get topics by chapter",
        description = "Retrieves all active topics for a specific chapter within an institute. All authenticated users can view topics."
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
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> getTopicsByChapter(
            @Parameter(description = "Chapter ID", required = true, example = "1")
            @PathVariable Long chapterId, 
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId) {
        try {
            log.info("Getting topics for chapter: {} in institute: {}", chapterId, instituteId);
            TopicListResponseDto topics = topicService.getTopicsByChapter(chapterId, instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Topics retrieved successfully", topics));
        } catch (Exception e) {
            log.error("Error getting topics by chapter", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get topics: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Search topics",
        description = "Searches for topics by name or code within a specific institute. All authenticated users can search topics."
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
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> searchTopics(
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId, 
            @Parameter(description = "Search query", required = true, example = "Arrays")
            @RequestParam String query) {
        try {
            log.info("Searching topics in institute: {} with query: {}", instituteId, query);
            TopicListResponseDto topics = topicService.searchTopics(instituteId, query);
            return ResponseEntity.ok(ApiResponseDto.success("Topics retrieved successfully", topics));
        } catch (Exception e) {
            log.error("Error searching topics", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search topics: " + e.getMessage()));
        }
    }

    @GetMapping
    @RequireRole({UserRole.SUPER_ADMIN})
    @Operation(
        summary = "Get all topics",
        description = "Retrieves all topics across all institutes. Only SUPER_ADMIN users can access this endpoint."
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
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponseDto> getAllTopics() {
        try {
            log.info("Getting all topics");
            TopicListResponseDto topics = topicService.getAllTopics();
            return ResponseEntity.ok(ApiResponseDto.success("Topics retrieved successfully", topics));
        } catch (Exception e) {
            log.error("Error getting all topics", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get topics: " + e.getMessage()));
        }
    }
}
