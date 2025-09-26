package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.request.BulkUploadQuestionsRequestDto;
import com.testpire.testpire.dto.request.CreateQuestionRequestDto;
import com.testpire.testpire.dto.request.UpdateQuestionRequestDto;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.dto.response.BulkUploadResponseDto;
import com.testpire.testpire.dto.response.QuestionListResponseDto;
import com.testpire.testpire.dto.response.QuestionResponseDto;
import com.testpire.testpire.enums.DifficultyLevel;
import com.testpire.testpire.enums.UserRole;
import com.testpire.testpire.service.CsvUploadService;
import com.testpire.testpire.service.QuestionService;
import com.testpire.testpire.util.JwksJwtUtil;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Question Management", description = "APIs for managing questions and options within topics")
@SecurityRequirement(name = "Bearer Authentication")
public class QuestionController {

    private final QuestionService questionService;
    private final CsvUploadService csvUploadService;
    private final JwksJwtUtil jwtUtil;

    @PostMapping
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Create a new question",
        description = "Creates a new question with options for a specific topic within an institute. Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can create questions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Question created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Question created successfully\", \"success\": true, \"data\": {\"id\": 1, \"text\": \"What is the capital of France?\", \"difficultyLevel\": \"EASY\", \"topicId\": 1, \"instituteId\": 1}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation failed or topic not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Topic not found with ID: 1\", \"success\": false, \"data\": null}"
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
    public ResponseEntity<ApiResponseDto> createQuestion(@Valid @RequestBody CreateQuestionRequestDto request) {
        try {
            log.info("Creating question for topic: {} in institute: {}", request.topicId(), request.instituteId());
            QuestionResponseDto question = questionService.createQuestion(request);
            return ResponseEntity.ok(ApiResponseDto.success("Question created successfully", question));
        } catch (Exception e) {
            log.error("Error creating question", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to create question: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Update an existing question",
        description = "Updates an existing question by ID. Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can update questions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Question updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - validation failed or question not found",
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
    public ResponseEntity<ApiResponseDto> updateQuestion(
            @Parameter(description = "Question ID", required = true, example = "1")
            @PathVariable Long id, 
            @Valid @RequestBody UpdateQuestionRequestDto request) {
        try {
            log.info("Updating question with ID: {}", id);
            QuestionResponseDto question = questionService.updateQuestion(id, request);
            return ResponseEntity.ok(ApiResponseDto.success("Question updated successfully", question));
        } catch (Exception e) {
            log.error("Error updating question", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to update question: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Delete a question",
        description = "Soft deletes a question by ID (sets active to false). All associated options are also deleted. Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can delete questions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Question deleted successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - question not found",
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
    public ResponseEntity<ApiResponseDto> deleteQuestion(
            @Parameter(description = "Question ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            log.info("Deleting question with ID: {}", id);
            questionService.deleteQuestion(id);
            return ResponseEntity.ok(ApiResponseDto.success("Question deleted successfully", null));
        } catch (Exception e) {
            log.error("Error deleting question", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to delete question: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get question by ID",
        description = "Retrieves a question by its ID. All authenticated users can view questions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Question retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - question not found",
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
    public ResponseEntity<ApiResponseDto> getQuestionById(
            @Parameter(description = "Question ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            log.info("Getting question with ID: {}", id);
            QuestionResponseDto question = questionService.getQuestionById(id);
            return ResponseEntity.ok(ApiResponseDto.success("Question retrieved successfully", question));
        } catch (Exception e) {
            log.error("Error getting question", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get question: " + e.getMessage()));
        }
    }

    @GetMapping("/topic/{topicId}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get questions by topic",
        description = "Retrieves all active questions for a specific topic within an institute. All authenticated users can view questions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Questions retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> getQuestionsByTopic(
            @Parameter(description = "Topic ID", required = true, example = "1")
            @PathVariable Long topicId,
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId) {
        try {
            log.info("Getting questions for topic: {} in institute: {}", topicId, instituteId);
            QuestionListResponseDto questions = questionService.getQuestionsByTopic(topicId, instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Questions retrieved successfully", questions));
        } catch (Exception e) {
            log.error("Error getting questions by topic", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get questions: " + e.getMessage()));
        }
    }

    @GetMapping("/institute/{instituteId}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get questions by institute",
        description = "Retrieves all active questions for a specific institute. All authenticated users can view questions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Questions retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> getQuestionsByInstitute(
            @Parameter(description = "Institute ID", required = true, example = "1")
            @PathVariable Long instituteId) {
        try {
            log.info("Getting questions for institute: {}", instituteId);
            QuestionListResponseDto questions = questionService.getQuestionsByInstitute(instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Questions retrieved successfully", questions));
        } catch (Exception e) {
            log.error("Error getting questions by institute", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get questions: " + e.getMessage()));
        }
    }

    @GetMapping("/difficulty/{difficultyLevel}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get questions by difficulty level",
        description = "Retrieves all active questions for a specific topic and difficulty level within an institute. All authenticated users can view questions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Questions retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> getQuestionsByDifficulty(
            @Parameter(description = "Difficulty Level", required = true, example = "EASY")
            @PathVariable DifficultyLevel difficultyLevel,
            @Parameter(description = "Topic ID", required = true, example = "1")
            @RequestParam Long topicId,
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId) {
        try {
            log.info("Getting questions for topic: {} in institute: {} with difficulty: {}", topicId, instituteId, difficultyLevel);
            QuestionListResponseDto questions = questionService.getQuestionsByDifficulty(topicId, instituteId, difficultyLevel);
            return ResponseEntity.ok(ApiResponseDto.success("Questions retrieved successfully", questions));
        } catch (Exception e) {
            log.error("Error getting questions by difficulty", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get questions: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Search questions",
        description = "Searches for questions by text or question type within a specific institute. All authenticated users can search questions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Questions retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> searchQuestions(
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId, 
            @Parameter(description = "Search query", required = true, example = "What is")
            @RequestParam String query) {
        try {
            log.info("Searching questions in institute: {} with query: {}", instituteId, query);
            QuestionListResponseDto questions = questionService.searchQuestions(instituteId, query);
            return ResponseEntity.ok(ApiResponseDto.success("Questions retrieved successfully", questions));
        } catch (Exception e) {
            log.error("Error searching questions", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search questions: " + e.getMessage()));
        }
    }

    @GetMapping("/search/topic/{topicId}")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Search questions by topic",
        description = "Searches for questions by text or question type within a specific topic and institute. All authenticated users can search questions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Questions retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> searchQuestionsByTopic(
            @Parameter(description = "Topic ID", required = true, example = "1")
            @PathVariable Long topicId,
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId, 
            @Parameter(description = "Search query", required = true, example = "What is")
            @RequestParam String query) {
        try {
            log.info("Searching questions for topic: {} in institute: {} with query: {}", topicId, instituteId, query);
            QuestionListResponseDto questions = questionService.searchQuestionsByTopic(topicId, instituteId, query);
            return ResponseEntity.ok(ApiResponseDto.success("Questions retrieved successfully", questions));
        } catch (Exception e) {
            log.error("Error searching questions by topic", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search questions: " + e.getMessage()));
        }
    }

    @PostMapping("/bulk-upload")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER})
    @Operation(
        summary = "Bulk upload questions from CSV",
        description = "Uploads multiple questions from a CSV file. The CSV should contain question text, image URLs, difficulty level, options, etc. Images are automatically uploaded to S3. Only users with SUPER_ADMIN, INST_ADMIN, or TEACHER roles can bulk upload questions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bulk upload completed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Bulk upload completed\", \"success\": true, \"data\": {\"totalProcessed\": 10, \"successfulUploads\": 8, \"failedUploads\": 2, \"errors\": [\"Row 3: Invalid difficulty level\", \"Row 7: Missing question text\"]}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - invalid CSV format or validation failed",
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
    public ResponseEntity<ApiResponseDto> bulkUploadQuestions(
            @Parameter(description = "CSV file containing questions", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Institute ID", required = true, example = "1")
            @RequestParam Long instituteId) {
        try {
            log.info("Bulk uploading questions for institute: {}",  instituteId);
            BulkUploadResponseDto result = csvUploadService.processBulkUpload(file, instituteId, "system");
            return ResponseEntity.ok(ApiResponseDto.success("Bulk upload completed", result));
        } catch (Exception e) {
            log.error("Error in bulk upload", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to process bulk upload: " + e.getMessage()));
        }
    }

    @GetMapping
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Get all questions",
        description = "Retrieves all questions across all institutes. Only SUPER_ADMIN users can access this endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Questions retrieved successfully",
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
    public ResponseEntity<ApiResponseDto> getAllQuestions() {
        try {
            log.info("Getting all questions");
            QuestionListResponseDto questions = questionService.getAllQuestions();
            return ResponseEntity.ok(ApiResponseDto.success("Questions retrieved successfully", questions));
        } catch (Exception e) {
            log.error("Error getting all questions", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get questions: " + e.getMessage()));
        }
    }
}

