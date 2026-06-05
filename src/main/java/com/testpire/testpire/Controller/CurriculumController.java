package com.testpire.testpire.Controller;

import com.testpire.testpire.annotation.RequirePermission;
import com.testpire.testpire.dto.response.ApiResponseDto;
import com.testpire.testpire.dto.response.CurriculumUploadResponseDto;
import com.testpire.testpire.enums.Permission;
import com.testpire.testpire.service.CurriculumUploadService;
import com.testpire.testpire.util.RequestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/curriculum")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Curriculum", description = "Bulk upload of subjects, chapters, and topics")
@SecurityRequirement(name = "bearerAuth")
public class CurriculumController {

    private final CurriculumUploadService curriculumUploadService;

    @PostMapping("/bulk-upload")
    @RequirePermission(Permission.CURRICULUM_BULK_UPLOAD)
    @Operation(
            summary = "Bulk upload curriculum from CSV",
            description = "Creates subjects, chapters, and topics from a single denormalized CSV file. "
                    + "Existing entities (matched by code) are reused. "
                    + "See CURRICULUM_UPLOAD_FORMAT.md for the column layout."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload completed (check errors[] for row-level failures)"),
            @ApiResponse(responseCode = "400", description = "Bad request — invalid CSV format or header mismatch"),
            @ApiResponse(responseCode = "403", description = "Forbidden — insufficient permissions")
    })
    public ResponseEntity<ApiResponseDto> bulkUploadCurriculum(
            @Parameter(description = "CSV file containing curriculum rows", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            Long instituteId = RequestUtils.getCurrentUserInstituteId();
            log.info("Bulk uploading curriculum for institute: {}", instituteId);
            CurriculumUploadResponseDto result =
                    curriculumUploadService.processBulkUpload(file, instituteId, RequestUtils.getCurrentUsername());
            return ResponseEntity.ok(ApiResponseDto.success("Curriculum upload completed", result));
        } catch (Exception e) {
            log.error("Error in curriculum bulk upload", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to process curriculum upload: " + e.getMessage()));
        }
    }
}
