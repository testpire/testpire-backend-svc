package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequireRole;
import com.testpire.testpire.dto.request.BulkUploadQuestionsRequestDto;
import com.testpire.testpire.dto.request.CreateQuestionRequestDto;
import com.testpire.testpire.dto.request.QuestionCriteriaDto;
import com.testpire.testpire.dto.request.QuestionSearchRequestDto;
import com.testpire.testpire.dto.request.PaginationRequestDto;
import com.testpire.testpire.dto.request.SortingRequestDto;
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
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Creating question for topic: {} in institute: {}", request.topicId(), instituteId);
            
            // Create a new request with instituteId from JWT
            CreateQuestionRequestDto requestWithInstituteId = CreateQuestionRequestDto.builder()
                    .text(request.text())
                    .difficultyLevel(request.difficultyLevel())
                    .questionType(request.questionType())
                    .marks(request.marks())
                    .negativeMarks(request.negativeMarks())
                    .explanation(request.explanation())
                    .questionImagePath(request.questionImagePath())
                    .topicId(request.topicId())
                    .instituteId(instituteId)
                    .options(request.options())
                    .build();
            
            QuestionResponseDto question = questionService.createQuestion(requestWithInstituteId);
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
            @PathVariable Long topicId) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
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
    public ResponseEntity<ApiResponseDto> getQuestionsByInstitute() {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Getting questions for institute: {}", instituteId);
            QuestionListResponseDto questions = questionService.getQuestionsByInstitute(instituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Questions retrieved successfully", questions));
        } catch (Exception e) {
            log.error("Error getting questions by institute", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to get questions: " + e.getMessage()));
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
            @RequestParam("file") MultipartFile file) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Bulk uploading questions for institute: {}", instituteId);
            BulkUploadResponseDto result = csvUploadService.processBulkUpload(file, instituteId, "system");
            return ResponseEntity.ok(ApiResponseDto.success("Bulk upload completed", result));
        } catch (Exception e) {
            log.error("Error in bulk upload", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to process bulk upload: " + e.getMessage()));
        }
    }

    @PostMapping("/search/advanced")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Advanced search for questions",
        description = "Performs advanced search for questions using multiple criteria including institute, course, subject, chapter, topic, difficulty level, question type, marks range, text search, and more. Supports pagination and sorting. All authenticated users can search questions."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Questions retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseDto.class),
                examples = @ExampleObject(
                    value = "{\"message\": \"Questions retrieved successfully\", \"success\": true, \"data\": {\"questions\": [...], \"totalCount\": 10}}"
                )
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
    public ResponseEntity<ApiResponseDto> searchQuestionsAdvanced(@Valid @RequestBody QuestionSearchRequestDto request) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Advanced search for questions with criteria: {}", request);
            
            // Set instituteId from JWT token
            QuestionCriteriaDto criteria = QuestionCriteriaDto.builder()
                    .instituteId(instituteId)
                    .courseId(request.getCourseId())
                    .subjectId(request.getSubjectId())
                    .chapterId(request.getChapterId())
                    .topicId(request.getTopicId())
                    .searchText(request.getSearchText())
                    .difficultyLevel(request.getDifficultyLevel())
                    .questionType(request.getQuestionType())
                    .minMarks(request.getMinMarks())
                    .maxMarks(request.getMaxMarks())
                    .minNegativeMarks(request.getMinNegativeMarks())
                    .maxNegativeMarks(request.getMaxNegativeMarks())
                    .hasQuestionImage(request.getHasQuestionImage())
                    .hasExplanation(request.getHasExplanation())
                    .hasCorrectOption(request.getHasCorrectOption())
                    .hasOptions(request.getHasOptions())
                    .minOptions(request.getMinOptions())
                    .maxOptions(request.getMaxOptions())
                    .active(request.getActive())
                    .createdAfter(request.getCreatedAfter())
                    .createdBefore(request.getCreatedBefore())
                    .createdBy(request.getCreatedBy())
                    .build();
                    
            QuestionSearchRequestDto requestWithInstituteId = QuestionSearchRequestDto.builder()
                    .criteria(criteria)
                    .pagination(request.getPagination())
                    .sorting(request.getSorting())
                    .build();
            
            QuestionListResponseDto questions = questionService.searchQuestionsWithSpecification(requestWithInstituteId);
            return ResponseEntity.ok(ApiResponseDto.success("Questions retrieved successfully", questions));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search questions: " + e.getMessage()));
        }
    }

    @GetMapping("/search/advanced")
    @RequireRole({UserRole.SUPER_ADMIN, UserRole.INST_ADMIN, UserRole.TEACHER, UserRole.STUDENT})
    @Operation(
        summary = "Advanced search for questions (GET)",
        description = "Performs advanced search for questions using query parameters. Supports filtering by institute, course, subject, chapter, topic, difficulty level, question type, marks range, text search, and more. Supports pagination and sorting. All authenticated users can search questions."
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
    public ResponseEntity<ApiResponseDto> searchQuestionsAdvancedGet(
            @Parameter(description = "Course ID (optional)", example = "1")
            @RequestParam(required = false) Long courseId,
            @Parameter(description = "Subject ID (optional)", example = "1")
            @RequestParam(required = false) Long subjectId,
            @Parameter(description = "Chapter ID (optional)", example = "1")
            @RequestParam(required = false) Long chapterId,
            @Parameter(description = "Topic ID (optional)", example = "1")
            @RequestParam(required = false) Long topicId,
            @Parameter(description = "Search text (optional)", example = "What is")
            @RequestParam(required = false) String searchText,
            @Parameter(description = "Difficulty Level (optional)", example = "EASY")
            @RequestParam(required = false) String difficultyLevel,
            @Parameter(description = "Question Type (optional)", example = "MCQ")
            @RequestParam(required = false) String questionType,
            @Parameter(description = "Minimum marks (optional)", example = "1")
            @RequestParam(required = false) Integer minMarks,
            @Parameter(description = "Maximum marks (optional)", example = "10")
            @RequestParam(required = false) Integer maxMarks,
            @Parameter(description = "Active status (optional)", example = "true")
            @RequestParam(required = false) Boolean active,
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
            log.info("Advanced search for questions with GET parameters");
            
            // Convert string difficultyLevel to enum
            DifficultyLevel difficultyLevelEnum = null;
            if (difficultyLevel != null && !difficultyLevel.isEmpty()) {
                try {
                    difficultyLevelEnum = DifficultyLevel.valueOf(difficultyLevel.toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid difficulty level: {}", difficultyLevel);
                }
            }
            
            QuestionCriteriaDto criteria = QuestionCriteriaDto.builder()
                    .instituteId(instituteId)
                    .courseId(courseId)
                    .subjectId(subjectId)
                    .chapterId(chapterId)
                    .topicId(topicId)
                    .searchText(searchText)
                    .difficultyLevel(difficultyLevelEnum)
                    .questionType(questionType)
                    .minMarks(minMarks)
                    .maxMarks(maxMarks)
                    .active(active)
                    .build();
                    
            PaginationRequestDto pagination = PaginationRequestDto.builder()
                    .page(page)
                    .size(size)
                    .build();
                    
            SortingRequestDto sorting = SortingRequestDto.builder()
                    .field(sortBy)
                    .direction(sortDirection)
                    .build();
                    
            QuestionSearchRequestDto request = QuestionSearchRequestDto.builder()
                    .criteria(criteria)
                    .pagination(pagination)
                    .sorting(sorting)
                    .build();
            
            QuestionListResponseDto questions = questionService.searchQuestionsWithSpecification(request);
            return ResponseEntity.ok(ApiResponseDto.success("Questions retrieved successfully", questions));
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Failed to search questions: " + e.getMessage()));
        }
    }

}


